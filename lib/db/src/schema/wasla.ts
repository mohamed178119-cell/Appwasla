import {
  boolean,
  text,
  timestamp,
  pgTable,
} from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const devicesTable = pgTable("wasla_devices", {
  id: text("id").primaryKey(),
  code: text("code").notNull().unique(),
  displayName: text("display_name").notNull().default("نسخة وصلة"),
  online: boolean("online").notNull().default(true),
  createdAt: timestamp("created_at", { withTimezone: true })
    .notNull()
    .defaultNow(),
});

export const chatsTable = pgTable("wasla_chats", {
  id: text("id").primaryKey(),
  type: text("type").notNull(),
  name: text("name").notNull(),
  createdAt: timestamp("created_at", { withTimezone: true })
    .notNull()
    .defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true })
    .notNull()
    .defaultNow()
    .$onUpdate(() => new Date()),
});

export const chatMembersTable = pgTable("wasla_chat_members", {
  id: text("id").primaryKey(),
  chatId: text("chat_id").notNull(),
  deviceId: text("device_id").notNull(),
  status: text("status").notNull().default("active"),
});

export const chatRequestsTable = pgTable("wasla_chat_requests", {
  id: text("id").primaryKey(),
  chatId: text("chat_id").notNull(),
  fromDeviceId: text("from_device_id").notNull(),
  toDeviceId: text("to_device_id").notNull(),
  status: text("status").notNull().default("pending"),
  createdAt: timestamp("created_at", { withTimezone: true })
    .notNull()
    .defaultNow(),
});

export const messagesTable = pgTable("wasla_messages", {
  id: text("id").primaryKey(),
  chatId: text("chat_id").notNull(),
  senderId: text("sender_id").notNull(),
  text: text("text").notNull(),
  createdAt: timestamp("created_at", { withTimezone: true })
    .notNull()
    .defaultNow(),
});

export const insertDeviceSchema = createInsertSchema(devicesTable).omit({
  createdAt: true,
});
export const insertChatSchema = createInsertSchema(chatsTable).omit({
  createdAt: true,
  updatedAt: true,
});
export const insertChatMemberSchema = createInsertSchema(
  chatMembersTable,
);
export const insertChatRequestSchema = createInsertSchema(
  chatRequestsTable,
).omit({ createdAt: true });
export const insertMessageSchema = createInsertSchema(messagesTable).omit({
  createdAt: true,
});

export type Device = z.infer<typeof insertDeviceSchema>;
export type Chat = typeof chatsTable.$inferSelect;
export type ChatMember = typeof chatMembersTable.$inferSelect;
export type ChatRequest = typeof chatRequestsTable.$inferSelect;
export type Message = typeof messagesTable.$inferSelect;