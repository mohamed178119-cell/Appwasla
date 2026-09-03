import { randomInt, randomUUID } from "node:crypto";
import { Router, type IRouter } from "express";
import { and, desc, eq, inArray, or } from "drizzle-orm";
import { db } from "@workspace/db";
import {
  chatMembersTable,
  chatRequestsTable,
  chatsTable,
  devicesTable,
  messagesTable,
} from "@workspace/db";
import {
  CreateChatRequestBody,
  CreateChatRequestParams,
  CreateChatRequestResponse,
  CreateGroupBody,
  CreateGroupParams,
  CreateGroupResponse,
  RegisterDeviceBody,
  RegisterDeviceResponse,
  RespondToChatRequestBody,
  RespondToChatRequestParams,
  RespondToChatRequestResponse,
  SendMessageBody,
  SendMessageParams,
  SendMessageResponse,
  SyncDeviceParams,
  SyncDeviceResponse,
  UpdatePresenceBody,
  UpdatePresenceParams,
  UpdatePresenceResponse,
} from "@workspace/api-zod";

const router: IRouter = Router();
const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

function newCode(): string {
  const part = (length: number) =>
    Array.from({ length }, () => alphabet[randomInt(alphabet.length)]).join("");
  return `${part(3)}-${part(3)}`;
}

function iso(value: Date | string): string {
  return new Date(value).toISOString();
}

function deviceDto(device: typeof devicesTable.$inferSelect) {
  return {
    id: device.id,
    code: device.code,
    displayName: device.displayName,
    online: device.online,
    createdAt: iso(device.createdAt),
  };
}

async function getDevice(deviceId: string) {
  const [device] = await db
    .select()
    .from(devicesTable)
    .where(eq(devicesTable.id, deviceId));
  return device;
}

async function getRequestDto(request: typeof chatRequestsTable.$inferSelect) {
  const [[from], [to]] = await Promise.all([
    db.select().from(devicesTable).where(eq(devicesTable.id, request.fromDeviceId)),
    db.select().from(devicesTable).where(eq(devicesTable.id, request.toDeviceId)),
  ]);
  return {
    id: request.id,
    chatId: request.chatId,
    fromDeviceId: request.fromDeviceId,
    toDeviceId: request.toDeviceId,
    fromCode: from?.code ?? "UNKNOWN",
    toCode: to?.code ?? "UNKNOWN",
    status: request.status as "pending" | "accepted" | "rejected",
    createdAt: iso(request.createdAt),
  };
}

async function getChatDto(chatId: string, deviceId: string) {
  const [chat] = await db
    .select()
    .from(chatsTable)
    .where(eq(chatsTable.id, chatId));
  if (!chat) return null;

  const [membership] = await db
    .select()
    .from(chatMembersTable)
    .where(
      and(
        eq(chatMembersTable.chatId, chatId),
        eq(chatMembersTable.deviceId, deviceId),
      ),
    );
  if (!membership) return null;

  const members = await db
    .select({ membership: chatMembersTable, device: devicesTable })
    .from(chatMembersTable)
    .innerJoin(devicesTable, eq(chatMembersTable.deviceId, devicesTable.id))
    .where(eq(chatMembersTable.chatId, chatId));

  const messages = await db
    .select({ message: messagesTable, sender: devicesTable })
    .from(messagesTable)
    .leftJoin(devicesTable, eq(messagesTable.senderId, devicesTable.id))
    .where(eq(messagesTable.chatId, chatId))
    .orderBy(messagesTable.createdAt);

  const chatName =
    chat.type === "direct"
      ? members.find(({ membership: member }) => member.deviceId !== deviceId)
          ?.device.displayName ?? chat.name
      : chat.name;

  return {
    id: chat.id,
    type: chat.type as "direct" | "group",
    name: chatName,
    status: membership.status as
      | "pending_incoming"
      | "pending_outgoing"
      | "active"
      | "rejected",
    members: members.map(({ membership: member, device }) => ({
      deviceId: member.deviceId,
      code: device.code,
      displayName: device.displayName,
      online: device.online,
    })),
    messages: messages.map(({ message, sender }) => ({
      id: message.id,
      chatId: message.chatId,
      senderId: message.senderId,
      senderCode: sender?.code ?? null,
      text: message.text,
      createdAt: iso(message.createdAt),
    })),
    updatedAt: iso(chat.updatedAt),
  };
}

router.post("/devices", async (req, res): Promise<void> => {
  const parsed = RegisterDeviceBody.safeParse(req.body ?? {});
  if (!parsed.success) {
    req.log.warn({ errors: parsed.error.message }, "Invalid device registration");
    res.status(400).json({ error: parsed.error.message });
    return;
  }

  let created: typeof devicesTable.$inferSelect | undefined;
  for (let attempt = 0; attempt < 5 && !created; attempt += 1) {
    const [device] = await db
      .insert(devicesTable)
      .values({
        id: randomUUID(),
        code: newCode(),
        displayName: parsed.data.displayName?.trim() || "نسخة وصلة",
        online: true,
      })
      .onConflictDoNothing({ target: devicesTable.code })
      .returning();
    created = device;
  }
  if (!created) {
    res.status(503).json({ error: "تعذر إنشاء كود جديد، حاول مرة أخرى" });
    return;
  }

  res.status(201).json(RegisterDeviceResponse.parse(deviceDto(created)));
});

router.get("/devices/:deviceId/sync", async (req, res): Promise<void> => {
  const params = SyncDeviceParams.safeParse(req.params);
  if (!params.success) {
    res.status(400).json({ error: params.error.message });
    return;
  }
  const device = await getDevice(params.data.deviceId);
  if (!device) {
    res.status(404).json({ error: "الجهاز غير موجود" });
    return;
  }

  const memberships = await db
    .select()
    .from(chatMembersTable)
    .where(eq(chatMembersTable.deviceId, device.id));
  const chats = (
    await Promise.all(
      memberships.map((membership) =>
        getChatDto(membership.chatId, device.id),
      ),
    )
  ).filter((chat): chat is NonNullable<typeof chat> => Boolean(chat));

  const requests = await db
    .select()
    .from(chatRequestsTable)
    .where(
      or(
        eq(chatRequestsTable.fromDeviceId, device.id),
        eq(chatRequestsTable.toDeviceId, device.id),
      ),
    )
    .orderBy(desc(chatRequestsTable.createdAt));
  const payload = {
    device: deviceDto(device),
    chats: chats.sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
    ),
    requests: await Promise.all(requests.map(getRequestDto)),
  };

  res.json(SyncDeviceResponse.parse(payload));
});

router.patch("/devices/:deviceId/presence", async (req, res): Promise<void> => {
  const params = UpdatePresenceParams.safeParse(req.params);
  const body = UpdatePresenceBody.safeParse(req.body);
  if (!params.success || !body.success) {
    res.status(400).json({
      error: !params.success
        ? params.error.message
        : body.error?.message ?? "بيانات غير صحيحة",
    });
    return;
  }
  const [device] = await db
    .update(devicesTable)
    .set({ online: body.data.online })
    .where(eq(devicesTable.id, params.data.deviceId))
    .returning();
  if (!device) {
    res.status(404).json({ error: "الجهاز غير موجود" });
    return;
  }
  res.json(UpdatePresenceResponse.parse(deviceDto(device)));
});

router.post("/devices/:deviceId/requests", async (req, res): Promise<void> => {
  const params = CreateChatRequestParams.safeParse(req.params);
  const body = CreateChatRequestBody.safeParse(req.body);
  if (!params.success || !body.success) {
    res.status(400).json({
      error: !params.success
        ? params.error.message
        : body.error?.message ?? "بيانات غير صحيحة",
    });
    return;
  }
  const sender = await getDevice(params.data.deviceId);
  const targetCode = body.data.targetCode.trim().toUpperCase();
  const [target] = await db
    .select()
    .from(devicesTable)
    .where(eq(devicesTable.code, targetCode));
  if (!sender || !target) {
    res.status(404).json({ error: "الكود غير موجود" });
    return;
  }
  if (sender.id === target.id) {
    res.status(400).json({ error: "لا يمكن بدء محادثة مع نفس النسخة" });
    return;
  }

  const existing = await db
    .select({ chat: chatsTable, member: chatMembersTable })
    .from(chatsTable)
    .innerJoin(chatMembersTable, eq(chatMembersTable.chatId, chatsTable.id))
    .where(
      and(
        eq(chatsTable.type, "direct"),
        eq(chatMembersTable.deviceId, sender.id),
      ),
    );
  const duplicate = existing.find((row) => row.member.deviceId === sender.id);
  if (duplicate) {
    const members = await db
      .select()
      .from(chatMembersTable)
      .where(eq(chatMembersTable.chatId, duplicate.chat.id));
    if (members.some((member) => member.deviceId === target.id)) {
      res.status(409).json({ error: "هذه المحادثة موجودة بالفعل" });
      return;
    }
  }

  const chatId = randomUUID();
  const requestId = randomUUID();
  await db.transaction(async (tx) => {
    await tx.insert(chatsTable).values({
      id: chatId,
      type: "direct",
      name: target.displayName,
    });
    await tx.insert(chatMembersTable).values([
      {
        id: randomUUID(),
        chatId,
        deviceId: sender.id,
        status: "pending_outgoing",
      },
      {
        id: randomUUID(),
        chatId,
        deviceId: target.id,
        status: "pending_incoming",
      },
    ]);
    await tx.insert(chatRequestsTable).values({
      id: requestId,
      chatId,
      fromDeviceId: sender.id,
      toDeviceId: target.id,
      status: "pending",
    });
  });
  const [request] = await db
    .select()
    .from(chatRequestsTable)
    .where(eq(chatRequestsTable.id, requestId));
  res
    .status(201)
    .json(CreateChatRequestResponse.parse(await getRequestDto(request)));
});

router.post("/devices/:deviceId/groups", async (req, res): Promise<void> => {
  const params = CreateGroupParams.safeParse(req.params);
  const body = CreateGroupBody.safeParse(req.body);
  if (!params.success || !body.success) {
    res.status(400).json({
      error: !params.success
        ? params.error.message
        : body.error?.message ?? "بيانات غير صحيحة",
    });
    return;
  }
  const owner = await getDevice(params.data.deviceId);
  const codes = [...new Set(body.data.memberCodes.map((code) => code.trim().toUpperCase()))];
  const members = await db
    .select()
    .from(devicesTable)
    .where(inArray(devicesTable.code, codes));
  if (!owner || members.length !== codes.length) {
    res.status(404).json({ error: "تأكد أن كل الأكواد صحيحة" });
    return;
  }

  const chatId = randomUUID();
  await db.transaction(async (tx) => {
    await tx.insert(chatsTable).values({
      id: chatId,
      type: "group",
      name: body.data.name.trim(),
    });
    await tx.insert(chatMembersTable).values([
      {
        id: randomUUID(),
        chatId,
        deviceId: owner.id,
        status: "active",
      },
      ...members
        .filter((member) => member.id !== owner.id)
        .map((member) => ({
          id: randomUUID(),
          chatId,
          deviceId: member.id,
          status: "active",
        })),
    ]);
  });

  const chat = await getChatDto(chatId, owner.id);
  res.status(201).json(CreateGroupResponse.parse(chat));
});

router.patch("/requests/:requestId", async (req, res): Promise<void> => {
  const params = RespondToChatRequestParams.safeParse(req.params);
  const body = RespondToChatRequestBody.safeParse(req.body);
  if (!params.success || !body.success) {
    res.status(400).json({
      error: !params.success
        ? params.error.message
        : body.error?.message ?? "بيانات غير صحيحة",
    });
    return;
  }
  const [request] = await db
    .select()
    .from(chatRequestsTable)
    .where(eq(chatRequestsTable.id, params.data.requestId));
  if (!request) {
    res.status(404).json({ error: "الطلب غير موجود" });
    return;
  }
  if (request.toDeviceId !== body.data.deviceId) {
    res.status(403).json({ error: "غير مسموح بتنفيذ هذا الطلب" });
    return;
  }
  if (request.status !== "pending") {
    res.status(409).json({ error: "تم التعامل مع هذا الطلب من قبل" });
    return;
  }

  await db.transaction(async (tx) => {
    await tx
      .update(chatRequestsTable)
      .set({ status: body.data.status })
      .where(eq(chatRequestsTable.id, request.id));
    await tx
      .update(chatMembersTable)
      .set({ status: body.data.status === "accepted" ? "active" : "rejected" })
      .where(eq(chatMembersTable.chatId, request.chatId));
    await tx
      .update(chatsTable)
      .set({ updatedAt: new Date() })
      .where(eq(chatsTable.id, request.chatId));
  });

  const [updated] = await db
    .select()
    .from(chatRequestsTable)
    .where(eq(chatRequestsTable.id, request.id));
  res
    .status(200)
    .json(RespondToChatRequestResponse.parse(await getRequestDto(updated)));
});

router.post("/chats/:chatId/messages", async (req, res): Promise<void> => {
  const params = SendMessageParams.safeParse(req.params);
  const body = SendMessageBody.safeParse(req.body);
  if (!params.success || !body.success) {
    res.status(400).json({
      error: !params.success
        ? params.error.message
        : body.error?.message ?? "بيانات غير صحيحة",
    });
    return;
  }
  const sender = await getDevice(body.data.deviceId);
  const [membership] = await db
    .select()
    .from(chatMembersTable)
    .where(
      and(
        eq(chatMembersTable.chatId, params.data.chatId),
        eq(chatMembersTable.deviceId, body.data.deviceId),
      ),
    );
  if (!sender || !membership) {
    res.status(404).json({ error: "المحادثة غير موجودة" });
    return;
  }
  if (membership.status !== "active") {
    res.status(403).json({ error: "المحادثة لم يتم قبولها بعد" });
    return;
  }

  const [message] = await db
    .insert(messagesTable)
    .values({
      id: randomUUID(),
      chatId: params.data.chatId,
      senderId: sender.id,
      text: body.data.text.trim(),
    })
    .returning();
  await db
    .update(chatsTable)
    .set({ updatedAt: new Date() })
    .where(eq(chatsTable.id, params.data.chatId));

  res.status(201).json(
    SendMessageResponse.parse({
      id: message.id,
      chatId: message.chatId,
      senderId: message.senderId,
      senderCode: sender.code,
      text: message.text,
      createdAt: iso(message.createdAt),
    }),
  );
});

export default router;