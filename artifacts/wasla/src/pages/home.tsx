import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  ArrowDownLeft,
  ArrowLeft,
  ArrowUpLeft,
  Check,
  CheckCheck,
  ChevronLeft,
  Copy,
  Info,
  LoaderCircle,
  MessageCircle,
  MoreHorizontal,
  Plus,
  Radio,
  RefreshCw,
  Send,
  Settings2,
  ShieldCheck,
  Sparkles,
  Users,
  X,
} from "lucide-react";
import {
  getHealthCheckQueryKey,
  getSyncDeviceQueryKey,
  useCreateChatRequest,
  useCreateGroup,
  useHealthCheck,
  useRegisterDevice,
  useRespondToChatRequest,
  useSendMessage,
  useSyncDevice,
  useUpdatePresence,
} from "@workspace/api-client-react";
import type {
  Chat,
  ChatRequest,
  Device,
  DeviceSync,
  Message,
} from "@workspace/api-client-react";
import { useQueryClient } from "@tanstack/react-query";

type Panel = "messages" | "requests" | "about";

function formatTime(value: string) {
  return new Intl.DateTimeFormat("ar-SA", {
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ar-SA", {
    day: "numeric",
    month: "long",
  }).format(new Date(value));
}

function initials(name: string) {
  return name.trim().slice(0, 2) || "و";
}

function Avatar({
  name,
  online = false,
  size = "md",
}: {
  name: string;
  online?: boolean;
  size?: "sm" | "md" | "lg";
}) {
  const sizeClass = size === "lg" ? "h-14 w-14 text-lg" : size === "sm" ? "h-9 w-9 text-xs" : "h-11 w-11 text-sm";
  return (
    <div className={`relative shrink-0 ${sizeClass}`}>
      <div className="flex h-full w-full items-center justify-center rounded-2xl bg-[linear-gradient(135deg,hsl(263_63%_70%/.78),hsl(239_58%_64%/.38))] font-display font-bold text-[#101b2d]">
        {initials(name)}
      </div>
      {online && <span className="absolute -bottom-0.5 -left-0.5 h-3 w-3 rounded-full border-2 border-[#0e1727] bg-[#52e2c7]" />}
    </div>
  );
}

function LoadingState({ label = "نرتّب مساحتك الخاصة..." }: { label?: string }) {
  return (
    <div className="flex min-h-[380px] flex-col items-center justify-center gap-4 text-center">
      <div className="relative">
        <div className="h-14 w-14 rounded-[20px] border border-[#263850] bg-[#16243a] p-3">
          <Radio className="h-full w-full text-[#52e2c7] wasla-pulse" />
        </div>
        <span className="absolute -right-1 -top-1 h-3 w-3 rounded-full bg-[#b8a5ff]" />
      </div>
      <p className="text-sm text-[#a8b5c8]">{label}</p>
    </div>
  );
}

function Notice({ text, onClose }: { text: string; onClose: () => void }) {
  return (
    <div className="fixed bottom-5 left-5 z-50 flex items-center gap-3 rounded-2xl border border-[#32556a] bg-[#162e3d] px-4 py-3 text-sm text-[#d8fff7] shadow-2xl shadow-[#07121e]/50 wasla-appear">
      <Check className="h-4 w-4 text-[#52e2c7]" />
      <span>{text}</span>
      <button data-testid="button-dismiss-notice" onClick={onClose} className="mr-1 rounded-lg p-1 text-[#86a0b4] hover:bg-[#213c4b] hover:text-white">
        <X className="h-3.5 w-3.5" />
      </button>
    </div>
  );
}

function Registration({
  register,
  pending,
  error,
}: {
  register: (name: string) => void;
  pending: boolean;
  error: boolean;
}) {
  const [name, setName] = useState("");
  return (
    <main className="wasla-shell relative flex min-h-[100dvh] items-center justify-center overflow-hidden px-5 py-10 text-right" dir="rtl">
      <div className="wasla-grid pointer-events-none absolute inset-0 opacity-60" />
      <div className="relative grid w-full max-w-5xl gap-12 md:grid-cols-[1fr_1.05fr] md:items-center md:gap-20">
        <section className="order-2 md:order-1">
          <div className="mb-8 flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#52e2c7] text-[#0e1727] shadow-lg shadow-[#52e2c7]/10">
              <Radio className="h-6 w-6" />
            </div>
            <span className="font-display text-2xl font-bold tracking-tight text-[#e7f5f4]">وصلة</span>
          </div>
          <p className="mb-4 text-sm font-semibold tracking-[.16em] text-[#52e2c7]">محادثات تبدأ منك</p>
          <h1 className="max-w-xl text-4xl font-bold leading-[1.25] text-[#e8edf7] sm:text-6xl">
            كود صغير،
            <br />
            <span className="text-[#b8a5ff]">باب خاص</span> لمن تعرف.
          </h1>
          <p className="mt-6 max-w-md text-base leading-8 text-[#9aaac0]">
            وصلة تمنح كل تثبيت كوداً ثابتاً. شاركه مع من تثق، وابدأ محادثة من أي جهاز، بدون حسابات طويلة أو قوائم معقدة.
          </p>
          <div className="mt-9 flex flex-wrap gap-3 text-sm text-[#a8b5c8]">
            <span className="flex items-center gap-2 rounded-full border border-[#263850] bg-[#121f33]/70 px-4 py-2.5"><ShieldCheck className="h-4 w-4 text-[#52e2c7]" /> خاص بك دائماً</span>
            <span className="flex items-center gap-2 rounded-full border border-[#263850] bg-[#121f33]/70 px-4 py-2.5"><Sparkles className="h-4 w-4 text-[#b8a5ff]" /> بسيط وواضح</span>
          </div>
        </section>
        <section className="order-1 rounded-[32px] border border-[#2b3e58] bg-[#121f33]/90 p-6 shadow-2xl shadow-[#07121e]/40 backdrop-blur md:order-2 md:p-9">
          <div className="mb-8 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold tracking-[.12em] text-[#71839b]">الخطوة الأولى</p>
              <h2 className="mt-2 text-2xl font-bold text-[#e8edf7]">عرّف نفسك لوَصلة</h2>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#1e3150] text-[#b8a5ff]"><ArrowUpLeft className="h-6 w-6" /></div>
          </div>
          <label htmlFor="display-name" className="mb-2 block text-sm font-semibold text-[#c3cedd]">الاسم الظاهر</label>
          <input
            id="display-name"
            data-testid="input-display-name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            onKeyDown={(event) => { if (event.key === "Enter") register(name); }}
            maxLength={60}
            placeholder="مثال: محمد"
            className="h-14 w-full rounded-2xl border border-[#2b3e58] bg-[#0d192b] px-4 text-base text-[#e8edf7] outline-none transition placeholder:text-[#65758b] focus:border-[#52e2c7] focus:ring-4 focus:ring-[#52e2c7]/10"
          />
          <button
            data-testid="button-create-space"
            disabled={pending || !name.trim()}
            onClick={() => register(name)}
            className="mt-4 flex h-14 w-full items-center justify-center gap-2 rounded-2xl bg-[#52e2c7] font-bold text-[#0e1727] transition hover:bg-[#79f0d9] disabled:cursor-not-allowed disabled:opacity-40"
          >
            {pending ? <LoaderCircle className="h-5 w-5 animate-spin" /> : <>أنشئ وصلة <ArrowLeft className="h-5 w-5" /></>}
          </button>
          {error && <p data-testid="status-registration-error" className="mt-4 rounded-xl bg-[#522d3b]/40 px-3 py-2 text-sm text-[#ffacb8]">تعذر إنشاء الوصلة الآن. جرّب مرة أخرى.</p>}
          <p className="mt-5 text-center text-xs leading-6 text-[#71839b]">لن نطلب بريداً إلكترونياً أو كلمة مرور.<br />كودك هو عنوانك في وصلة.</p>
        </section>
      </div>
    </main>
  );
}

function MessageBubble({ message, mine }: { message: Message; mine: boolean }) {
  return (
    <div className={`flex ${mine ? "justify-start" : "justify-end"} wasla-appear`}>
      <div className={`max-w-[78%] ${mine ? "items-start" : "items-end"} flex flex-col`}>
        <div className={`rounded-[20px] px-4 py-3 text-[15px] leading-7 ${mine ? "rounded-bl-md bg-[#3b4965] text-[#f1f5fb]" : "rounded-br-md bg-[#52e2c7] text-[#0d2530]"}`}>
          {message.text}
        </div>
        <div className={`mt-1.5 flex items-center gap-1.5 px-1 text-[11px] text-[#6f8097] ${mine ? "flex-row-reverse" : ""}`}>
          <span>{formatTime(message.createdAt)}</span>
          {mine && <CheckCheck className="h-3.5 w-3.5 text-[#52e2c7]" />}
        </div>
      </div>
    </div>
  );
}

function ChatRow({ chat, active, onClick, ownId }: { chat: Chat; active: boolean; onClick: () => void; ownId: string }) {
  const other = chat.members.find((member) => member.deviceId !== ownId) ?? chat.members[0];
  const last = chat.messages[chat.messages.length - 1];
  return (
    <button
      data-testid={`button-chat-${chat.id}`}
      onClick={onClick}
      className={`group flex w-full items-center gap-3 rounded-2xl p-3 text-right transition ${active ? "bg-[#223753] shadow-lg shadow-[#091321]/30" : "hover:bg-[#182b44]"}`}
    >
      {chat.type === "group" ? <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-[#352e63] text-[#c8baff]"><Users className="h-5 w-5" /></div> : <Avatar name={other?.displayName ?? chat.name} online={other?.online} />}
      <div className="min-w-0 flex-1">
        <div className="flex items-center justify-between gap-2">
          <p className={`truncate text-sm font-semibold ${active ? "text-[#eff5ff]" : "text-[#c5d0df]"}`}>{chat.name}</p>
          {last && <time className="shrink-0 text-[10px] text-[#72849b]">{formatTime(last.createdAt)}</time>}
        </div>
        <p className="mt-1 truncate text-xs text-[#71839b]">{last?.text ?? (chat.status === "pending_outgoing" ? "بانتظار الموافقة" : "لا توجد رسائل بعد")}</p>
      </div>
      {chat.status !== "active" && <span className={`h-2 w-2 rounded-full ${chat.status === "pending_incoming" ? "bg-[#b8a5ff]" : "bg-[#6c7b90]"}`} />}
    </button>
  );
}

function Modal({
  title,
  description,
  onClose,
  children,
}: {
  title: string;
  description: string;
  onClose: () => void;
  children: ReactNode;
}) {
  return (
    <div className="fixed inset-0 z-40 flex items-end justify-center bg-[#07111f]/75 p-0 backdrop-blur-sm sm:items-center sm:p-5">
      <div role="dialog" aria-modal="true" className="w-full max-w-md rounded-t-[28px] border border-[#2b3e58] bg-[#14243a] p-6 shadow-2xl shadow-[#07121e]/60 sm:rounded-[28px] sm:p-7 wasla-appear">
        <div className="mb-6 flex items-start justify-between gap-4">
          <div><h2 className="text-xl font-bold text-[#eef4fc]">{title}</h2><p className="mt-2 text-sm leading-6 text-[#8ea0b7]">{description}</p></div>
          <button data-testid="button-close-modal" onClick={onClose} className="rounded-xl p-2 text-[#8395ad] hover:bg-[#20354f] hover:text-white"><X className="h-5 w-5" /></button>
        </div>
        {children}
      </div>
    </div>
  );
}

function RequestsPanel({
  requests,
  chats,
  respond,
  pending,
  ownId,
}: {
  requests: ChatRequest[];
  chats: Chat[];
  respond: (id: string, status: "accepted" | "rejected") => void;
  pending: boolean;
  ownId: string;
}) {
  const incoming = requests.filter((request) => request.status === "pending" && request.toDeviceId === ownId);
  const outgoing = requests.filter((request) => request.status === "pending" && request.fromDeviceId === ownId);
  return (
    <section className="wasla-appear">
      <div className="mb-8 flex items-end justify-between">
        <div><p className="text-sm font-semibold text-[#52e2c7]">الوصولات</p><h1 className="mt-2 text-3xl font-bold text-[#eef4fc]">طلبات المحادثة</h1></div>
        <span className="rounded-full bg-[#293860] px-3 py-1 text-xs text-[#c8baff]">{incoming.length + outgoing.length} معلّقة</span>
      </div>
      {incoming.length === 0 && outgoing.length === 0 ? (
        <div className="flex min-h-[390px] flex-col items-center justify-center rounded-[28px] border border-dashed border-[#2b3e58] bg-[#121f33]/50 px-7 text-center">
          <div className="mb-5 flex h-16 w-16 items-center justify-center rounded-[22px] bg-[#213451] text-[#b8a5ff]"><ShieldCheck className="h-7 w-7" /></div>
          <h2 className="text-lg font-bold text-[#dfe8f5]">لا طلبات معلّقة</h2>
          <p className="mt-2 max-w-xs text-sm leading-7 text-[#7f91a9]">حين يطلب أحدهم محادثتك، ستظهر هنا لتختار من يدخل إلى بابك.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {incoming.map((request) => {
            const chat = chats.find((item) => item.id === request.chatId);
            return (
              <div key={request.id} data-testid={`card-request-${request.id}`} className="rounded-[24px] border border-[#2b3e58] bg-[#14243a] p-5">
                <div className="flex items-center gap-3"><Avatar name={request.fromCode} size="md" /><div><p className="font-semibold text-[#edf4fc]">دعوة من كود {request.fromCode}</p><p className="mt-1 text-xs text-[#778aa2]">{formatDate(request.createdAt)} {chat?.name ? `• ${chat.name}` : ""}</p></div></div>
                <p className="mt-5 text-sm leading-6 text-[#9aabc0]">هذا الشخص يريد فتح محادثة خاصة معك. القرار بيدك تماماً.</p>
                <div className="mt-5 flex gap-2">
                  <button data-testid={`button-accept-request-${request.id}`} disabled={pending} onClick={() => respond(request.id, "accepted")} className="flex h-11 flex-1 items-center justify-center gap-2 rounded-xl bg-[#52e2c7] text-sm font-bold text-[#0e1727] hover:bg-[#79f0d9] disabled:opacity-50"><Check className="h-4 w-4" /> قبول</button>
                  <button data-testid={`button-reject-request-${request.id}`} disabled={pending} onClick={() => respond(request.id, "rejected")} className="flex h-11 flex-1 items-center justify-center gap-2 rounded-xl border border-[#3b4b62] text-sm font-semibold text-[#b3c0d1] hover:bg-[#1c2e47] disabled:opacity-50"><X className="h-4 w-4" /> تجاهل</button>
                </div>
              </div>
            );
          })}
          {outgoing.map((request) => (
            <div key={request.id} data-testid={`card-outgoing-request-${request.id}`} className="rounded-[24px] border border-[#2b3e58] bg-[#14243a] p-5">
              <div className="flex items-center gap-3"><Avatar name={request.toCode} size="md" /><div><p className="font-semibold text-[#edf4fc]">دعوة إلى كود {request.toCode}</p><p className="mt-1 text-xs text-[#778aa2]">{formatDate(request.createdAt)}</p></div></div>
              <div className="mt-5 flex items-center gap-2 rounded-xl bg-[#202c48] px-3 py-3 text-xs text-[#aeb9d0]"><LoaderCircle className="h-4 w-4 animate-spin text-[#b8a5ff]" /> بانتظار موافقة الطرف الآخر</div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function AboutPanel({ device, health }: { device: Device; health?: string }) {
  return (
    <section className="wasla-appear">
      <div className="mb-8"><p className="text-sm font-semibold text-[#52e2c7]">عن وصلة</p><h1 className="mt-2 text-3xl font-bold text-[#eef4fc]">مساحتك، على طريقتك</h1></div>
      <div className="overflow-hidden rounded-[28px] border border-[#2b3e58] bg-[#14243a]">
        <div className="relative overflow-hidden p-7 sm:p-9">
          <div className="wasla-grid absolute inset-0 opacity-50" />
          <div className="relative">
            <div className="flex h-16 w-16 items-center justify-center rounded-[22px] bg-[#52e2c7] text-[#0e1727] shadow-xl shadow-[#52e2c7]/10"><Radio className="h-8 w-8" /></div>
            <h2 className="mt-7 max-w-md text-3xl font-bold leading-[1.35] text-[#eef4fc]">كل كود هو<br /><span className="text-[#b8a5ff]">وصلة لا تُنسى.</span></h2>
            <p className="mt-5 max-w-lg text-sm leading-8 text-[#9aabc0]">وصلة مساحة محادثة هادئة، صممها <strong className="font-semibold text-[#d8e1ee]">محمد سعد</strong> لتكون طريقة أقرب للتواصل: كود واحد ثابت، وأبواب لا تُفتح إلا بإذنك.</p>
          </div>
        </div>
        <div className="grid divide-y divide-[#2b3e58] border-t border-[#2b3e58] sm:grid-cols-2 sm:divide-x sm:divide-y-0 sm:divide-x-reverse">
          <div className="p-5"><p className="text-xs text-[#778aa2]">كود هذه الوصلة</p><p data-testid="text-about-code" className="mt-2 font-display text-xl font-bold tracking-[.12em] text-[#52e2c7]">{device.code}</p></div>
          <div className="p-5"><p className="text-xs text-[#778aa2]">إشارة الخادم</p><p data-testid="status-health" className="mt-2 flex items-center gap-2 font-semibold text-[#dce7f4]"><span className={`h-2 w-2 rounded-full ${health === "ok" ? "bg-[#52e2c7]" : "bg-[#b8a5ff]"}`} />{health === "ok" ? "كل شيء يعمل" : "جاري التحقق"}</p></div>
        </div>
      </div>
      <p className="mt-6 text-center text-xs text-[#63758d]">صُنع بعناية للعلاقات التي تستحق مكاناً خاصاً</p>
    </section>
  );
}

export default function Home() {
  const queryClient = useQueryClient();
  const [deviceId, setDeviceId] = useState<string | null>(() => localStorage.getItem("wasla-device-id"));
  const [panel, setPanel] = useState<Panel>("messages");
  const [selectedChatId, setSelectedChatId] = useState<string | null>(null);
  const [showRequest, setShowRequest] = useState(false);
  const [showGroup, setShowGroup] = useState(false);
  const [notice, setNotice] = useState("");
  const [message, setMessage] = useState("");
  const [targetCode, setTargetCode] = useState("");
  const [groupName, setGroupName] = useState("");
  const [memberCodes, setMemberCodes] = useState("");

  const health = useHealthCheck({ query: { queryKey: getHealthCheckQueryKey(), refetchInterval: 30000 } });
  const registerMutation = useRegisterDevice();
  const sync = useSyncDevice(deviceId ?? "", { query: { enabled: Boolean(deviceId), queryKey: getSyncDeviceQueryKey(deviceId ?? ""), refetchInterval: 15000 } });
  const updatePresence = useUpdatePresence();
  const createRequest = useCreateChatRequest();
  const createGroup = useCreateGroup();
  const respondRequest = useRespondToChatRequest();
  const sendMessage = useSendMessage();
  const syncData = sync.data as DeviceSync | undefined;
  const device = syncData?.device;
  const chats = syncData?.chats ?? [];
  const requests = syncData?.requests ?? [];
  const activeChat = chats.find((chat) => chat.id === selectedChatId) ?? null;
  const activeChats = useMemo(() => chats.filter((chat) => chat.status === "active" || chat.status === "pending_outgoing"), [chats]);
  const pendingIncoming = requests.filter((request) => request.status === "pending" && request.toDeviceId === deviceId);

  useEffect(() => {
    if (!selectedChatId && activeChats[0]) setSelectedChatId(activeChats[0].id);
    if (selectedChatId && !chats.some((chat) => chat.id === selectedChatId)) setSelectedChatId(activeChats[0]?.id ?? null);
  }, [activeChats, chats, selectedChatId]);

  useEffect(() => {
    if (!notice) return;
    const timer = window.setTimeout(() => setNotice(""), 3200);
    return () => window.clearTimeout(timer);
  }, [notice]);

  const invalidateSync = () => {
    if (deviceId) queryClient.invalidateQueries({ queryKey: getSyncDeviceQueryKey(deviceId) });
  };

  const register = (displayName: string) => {
    if (!displayName.trim()) return;
    registerMutation.mutate({ data: { displayName: displayName.trim() } }, {
      onSuccess: (newDevice) => {
        localStorage.setItem("wasla-device-id", newDevice.id);
        setDeviceId(newDevice.id);
        setNotice("تم إنشاء وصْلتك الخاصة");
      },
    });
  };

  const copyCode = async () => {
    if (!device?.code) return;
    await navigator.clipboard?.writeText(device.code);
    setNotice("تم نسخ الكود — شاركه مع من تثق");
  };

  const togglePresence = () => {
    if (!deviceId || !device) return;
    updatePresence.mutate({ deviceId, data: { online: !device.online } }, { onSuccess: () => { invalidateSync(); setNotice(device.online ? "ظهورك الآن مخفي" : "أنت متصل الآن"); } });
  };

  const submitRequest = () => {
    if (!deviceId || targetCode.trim().length < 4) return;
    createRequest.mutate({ deviceId, data: { targetCode: targetCode.trim() } }, {
      onSuccess: () => { setShowRequest(false); setTargetCode(""); invalidateSync(); setNotice("أُرسلت الدعوة — بانتظار الرد"); },
    });
  };

  const submitGroup = () => {
    if (!deviceId || !groupName.trim() || !memberCodes.trim()) return;
    createGroup.mutate({ deviceId, data: { name: groupName.trim(), memberCodes: memberCodes.split(/[\s,،]+/).map((code) => code.trim()).filter(Boolean) } }, {
      onSuccess: (chat) => { setShowGroup(false); setGroupName(""); setMemberCodes(""); invalidateSync(); setSelectedChatId(chat.id); setNotice("تم إنشاء المجموعة"); },
    });
  };

  const respond = (requestId: string, status: "accepted" | "rejected") => {
    if (!deviceId) return;
    respondRequest.mutate({ requestId, data: { deviceId, status } }, { onSuccess: () => { invalidateSync(); setNotice(status === "accepted" ? "أصبحت المحادثة مفتوحة" : "تم تجاهل الطلب"); } });
  };

  const submitMessage = () => {
    if (!deviceId || !activeChat || activeChat.status !== "active" || !message.trim()) return;
    sendMessage.mutate({ chatId: activeChat.id, data: { deviceId, text: message.trim() } }, { onSuccess: () => { setMessage(""); invalidateSync(); } });
  };

  if (!deviceId) return <Registration register={register} pending={registerMutation.isPending} error={registerMutation.isError} />;
  if (sync.isLoading) return <main className="wasla-shell min-h-[100dvh]" dir="rtl"><LoadingState /></main>;
  if (sync.isError || !device) {
    return (
      <main className="wasla-shell flex min-h-[100dvh] items-center justify-center px-5 text-center" dir="rtl">
        <div className="max-w-sm rounded-[28px] border border-[#2b3e58] bg-[#14243a] p-8">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#522d3b]/50 text-[#ffacb8]"><RefreshCw className="h-6 w-6" /></div>
          <h1 className="mt-5 text-xl font-bold text-[#edf4fc]">لم نتمكن من الوصول إلى وصْلتك</h1>
          <p className="mt-3 text-sm leading-7 text-[#8ea0b7]">قد يكون الاتصال انقطع. بياناتك محفوظة، أعد المحاولة فقط.</p>
          <button data-testid="button-retry-sync" onClick={() => sync.refetch()} className="mt-6 flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-[#52e2c7] font-bold text-[#0e1727]"><RefreshCw className="h-4 w-4" /> إعادة المحاولة</button>
        </div>
      </main>
    );
  }

  return (
    <main className="wasla-shell min-h-[100dvh] text-right" dir="rtl">
      <div className="wasla-grid pointer-events-none fixed inset-0 opacity-30" />
      <div className="relative mx-auto flex min-h-[100dvh] max-w-[1500px] overflow-hidden border-x border-[#1d2c42] bg-[#0f1a2b]/60 shadow-2xl shadow-[#07121e]/30">
        <aside className="hidden w-[290px] shrink-0 flex-col border-l border-[#22334c] bg-[#101d31]/90 lg:flex">
          <div className="flex items-center gap-3 px-6 pb-7 pt-8">
            <div className="flex h-10 w-10 items-center justify-center rounded-[14px] bg-[#52e2c7] text-[#0e1727]"><Radio className="h-5 w-5" /></div>
            <div><p className="font-display text-xl font-bold text-[#eef4fc]">وصلة</p><p className="text-[10px] tracking-[.14em] text-[#71839b]">PRIVATE SIGNAL</p></div>
          </div>
          <div className="px-4">
            <p className="mb-3 px-3 text-[10px] font-bold tracking-[.16em] text-[#60738d]">المساحة</p>
            <nav className="space-y-1">
              <button data-testid="button-nav-messages" onClick={() => setPanel("messages")} className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition ${panel === "messages" ? "bg-[#203650] text-[#eef4fc]" : "text-[#8193aa] hover:bg-[#182b44] hover:text-[#cbd7e6]"}`}><MessageCircle className="h-[18px] w-[18px]" />المحادثات<span className="mr-auto rounded-full bg-[#293c5f] px-2 py-0.5 text-[10px] text-[#b8a5ff]">{activeChats.length}</span></button>
              <button data-testid="button-nav-requests" onClick={() => setPanel("requests")} className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition ${panel === "requests" ? "bg-[#203650] text-[#eef4fc]" : "text-[#8193aa] hover:bg-[#182b44] hover:text-[#cbd7e6]"}`}><ArrowDownLeft className="h-[18px] w-[18px]" />الطلبات{pendingIncoming.length > 0 && <span className="mr-auto flex h-5 min-w-5 items-center justify-center rounded-full bg-[#b8a5ff] px-1.5 text-[10px] font-bold text-[#17152f]">{pendingIncoming.length}</span>}</button>
              <button data-testid="button-nav-about" onClick={() => setPanel("about")} className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition ${panel === "about" ? "bg-[#203650] text-[#eef4fc]" : "text-[#8193aa] hover:bg-[#182b44] hover:text-[#cbd7e6]"}`}><Info className="h-[18px] w-[18px]" />عن وصلة</button>
            </nav>
          </div>
          <div className="mt-auto p-4">
            <div className="rounded-2xl border border-[#263850] bg-[#14243a] p-4">
              <div className="flex items-center gap-3"><Avatar name={device.displayName} online={device.online} size="sm" /><div className="min-w-0"><p className="truncate text-sm font-semibold text-[#dfe8f5]">{device.displayName}</p><p className="font-display text-[11px] tracking-wider text-[#71839b]">{device.code}</p></div><button data-testid="button-toggle-presence" onClick={togglePresence} title="تغيير حالة الظهور" className="mr-auto rounded-lg p-1.5 text-[#71839b] hover:bg-[#213451] hover:text-[#52e2c7]"><Settings2 className="h-4 w-4" /></button></div>
              <button data-testid="button-presence-status" onClick={togglePresence} className="mt-4 flex items-center gap-2 text-xs text-[#93a5ba]"><span className={`h-2 w-2 rounded-full ${device.online ? "bg-[#52e2c7]" : "bg-[#66758a]"}`} />{device.online ? "متصل الآن" : "غير متصل"}<span className="mr-auto text-[10px] text-[#64778f]">تغيير</span></button>
            </div>
          </div>
        </aside>

        <section className="flex min-w-0 flex-1 flex-col md:flex-row">
          <div className="flex items-center justify-between border-b border-[#22334c] bg-[#101d31] px-4 py-3 lg:hidden">
            <div className="flex items-center gap-2"><div className="flex h-8 w-8 items-center justify-center rounded-xl bg-[#52e2c7] text-[#0e1727]"><Radio className="h-4 w-4" /></div><span className="font-display font-bold text-[#e9f1fa]">وصلة</span></div>
            <nav className="flex items-center gap-1">
              <button data-testid="button-mobile-nav-messages" onClick={() => setPanel("messages")} className={`rounded-lg px-3 py-2 text-xs font-semibold ${panel === "messages" ? "bg-[#203650] text-[#eef4fc]" : "text-[#8193aa]"}`}>المحادثات</button>
              <button data-testid="button-mobile-nav-requests" onClick={() => setPanel("requests")} className={`relative rounded-lg px-3 py-2 text-xs font-semibold ${panel === "requests" ? "bg-[#203650] text-[#eef4fc]" : "text-[#8193aa]"}`}>الطلبات{pendingIncoming.length > 0 && <span className="mr-1 inline-flex h-4 min-w-4 items-center justify-center rounded-full bg-[#b8a5ff] px-1 text-[9px] text-[#17152f]">{pendingIncoming.length}</span>}</button>
              <button data-testid="button-mobile-nav-about" onClick={() => setPanel("about")} className={`rounded-lg px-3 py-2 text-xs font-semibold ${panel === "about" ? "bg-[#203650] text-[#eef4fc]" : "text-[#8193aa]"}`}>عن وصلة</button>
            </nav>
          </div>
          <div className={`${panel !== "messages" ? "hidden md:flex" : "flex"} w-full shrink-0 flex-col border-l border-[#22334c] bg-[#111e31]/85 md:w-[340px]`}>
            <header className="flex items-center justify-between px-5 pb-5 pt-7">
              <div><p className="text-xs font-semibold text-[#52e2c7]">صباح الوصل</p><h1 className="mt-1 text-2xl font-bold text-[#edf4fc]">محادثاتك</h1></div>
              <button data-testid="button-open-request" onClick={() => setShowRequest(true)} className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#203650] text-[#52e2c7] transition hover:bg-[#294867]" title="طلب محادثة"><Plus className="h-5 w-5" /></button>
            </header>
            <div className="mx-5 mb-4 rounded-2xl border border-[#2a4560] bg-[#172c42] p-4">
              <div className="flex items-center justify-between"><p className="text-xs text-[#8da3ba]">كودك الخاص</p><button data-testid="button-copy-code" onClick={copyCode} className="flex items-center gap-1.5 text-xs font-semibold text-[#52e2c7] hover:text-[#8affdf]"><Copy className="h-3.5 w-3.5" /> نسخ</button></div>
              <div className="mt-2 flex items-center justify-between"><span data-testid="text-device-code" className="font-display text-2xl font-bold tracking-[.18em] text-[#e8f7f4]">{device.code}</span><span className="text-[#52e2c7]"><ArrowUpLeft className="h-5 w-5" /></span></div>
            </div>
            <div className="flex items-center justify-between px-5 pb-3"><p className="text-xs font-semibold text-[#6f8199]">آخر الوصلات</p><button data-testid="button-open-group" onClick={() => setShowGroup(true)} className="flex items-center gap-1 text-xs font-semibold text-[#b8a5ff]"><Users className="h-3.5 w-3.5" /> مجموعة جديدة</button></div>
            <div className="chat-scroll flex-1 space-y-1 overflow-y-auto px-3 pb-4">
              {activeChats.length === 0 ? <div className="px-4 py-12 text-center"><div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#1d304b] text-[#b8a5ff]"><MessageCircle className="h-6 w-6" /></div><p className="mt-4 text-sm font-semibold text-[#bac8d8]">ابدأ أول وصلة</p><p className="mt-2 text-xs leading-6 text-[#71839b]">اطلب محادثة بالكود من علامة الزائد أعلاه.</p></div> : activeChats.map((chat) => <ChatRow key={chat.id} chat={chat} ownId={device.id} active={chat.id === activeChat?.id} onClick={() => setSelectedChatId(chat.id)} />)}
            </div>
          </div>

          <div className="min-w-0 flex-1">
            <div className="mx-auto h-full max-w-[800px] px-5 py-7 sm:px-8 lg:px-12">
              {panel === "requests" && <RequestsPanel requests={requests} chats={chats} ownId={device.id} respond={respond} pending={respondRequest.isPending} />}
              {panel === "about" && <AboutPanel device={device} health={health.data?.status} />}
              {panel === "messages" && (activeChat ? (
                <div className="flex h-[calc(100dvh-3.5rem)] flex-col wasla-appear">
                  <header className="flex items-center gap-3 border-b border-[#22334c] pb-5">
                    <button data-testid="button-back-chats" onClick={() => setSelectedChatId(null)} className="rounded-xl p-2 text-[#8193aa] hover:bg-[#1d304b] md:hidden"><ChevronLeft className="h-5 w-5" /></button>
                    {activeChat.type === "group" ? <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#352e63] text-[#c8baff]"><Users className="h-5 w-5" /></div> : <Avatar name={activeChat.name} online={activeChat.members.some((member) => member.online && member.deviceId !== device.id)} />}
                    <div className="min-w-0 flex-1"><h2 data-testid="text-active-chat-name" className="truncate text-lg font-bold text-[#edf4fc]">{activeChat.name}</h2><p className="mt-0.5 text-xs text-[#73869f]">{activeChat.type === "group" ? `${activeChat.members.length} أعضاء` : activeChat.status === "active" ? "محادثة خاصة" : "بانتظار الموافقة"}</p></div>
                    <button data-testid="button-chat-more" onClick={() => setNotice("المحادثة محمية بكود الطرفين")} className="rounded-xl p-2 text-[#8193aa] hover:bg-[#1d304b]"><MoreHorizontal className="h-5 w-5" /></button>
                  </header>
                  <div className="chat-scroll flex-1 space-y-5 overflow-y-auto py-7">
                    <div className="mx-auto flex max-w-sm items-center gap-3 text-center text-[11px] text-[#647890]"><span className="h-px flex-1 bg-[#22334c]" /><span>بداية المحادثة</span><span className="h-px flex-1 bg-[#22334c]" /></div>
                    {activeChat.messages.length === 0 ? <div className="flex flex-1 flex-col items-center justify-center py-16 text-center"><div className="mb-4 flex h-16 w-16 items-center justify-center rounded-[22px] bg-[#193149] text-[#52e2c7]"><Send className="h-6 w-6 -rotate-12" /></div><h3 className="font-bold text-[#cbd7e5]">قل أول كلمة</h3><p className="mt-2 text-sm text-[#71839b]">لا تحتاج وصلة جيدة إلى مقدمة طويلة.</p></div> : activeChat.messages.map((item) => <MessageBubble key={item.id} message={item} mine={item.senderId === device.id} />)}
                  </div>
                  {activeChat.status === "active" ? <div className="border-t border-[#22334c] pt-5"><div className="flex items-end gap-2 rounded-[20px] border border-[#2b3e58] bg-[#14243a] p-2 focus-within:border-[#52e2c7]/70"><textarea data-testid="input-message" value={message} onChange={(event) => setMessage(event.target.value)} onKeyDown={(event) => { if (event.key === "Enter" && !event.shiftKey) { event.preventDefault(); submitMessage(); } }} rows={1} maxLength={4000} placeholder="اكتب رسالتك..." className="max-h-28 min-h-11 flex-1 resize-none bg-transparent px-3 py-2.5 text-sm leading-6 text-[#edf4fc] outline-none placeholder:text-[#657890]" /><button data-testid="button-send-message" disabled={sendMessage.isPending || !message.trim()} onClick={submitMessage} className="flex h-11 w-11 shrink-0 items-center justify-center rounded-[15px] bg-[#52e2c7] text-[#0e1727] transition hover:bg-[#79f0d9] disabled:cursor-not-allowed disabled:opacity-35"><Send className="h-4 w-4 -rotate-12" /></button></div><p className="mt-2 px-2 text-[10px] text-[#60738c]">Enter للإرسال · Shift + Enter لسطر جديد</p></div> : <div className="mt-auto rounded-2xl border border-[#39345e] bg-[#201e3d] p-4 text-center"><p className="text-sm font-semibold text-[#d2ccff]">هذه الوصلة بانتظار الموافقة</p><p className="mt-1 text-xs text-[#8b87ac]">ستتمكن من إرسال الرسائل بعد قبول الطرف الآخر.</p></div>}
                </div>
              ) : <div className="flex h-full min-h-[500px] flex-col items-center justify-center text-center"><div className="mb-6 flex h-20 w-20 items-center justify-center rounded-[28px] border border-[#2c4661] bg-[#172b44] text-[#52e2c7]"><MessageCircle className="h-8 w-8" /></div><h2 className="text-2xl font-bold text-[#e7eef8]">مساحتك هادئة هنا</h2><p className="mt-3 max-w-xs text-sm leading-7 text-[#8294ab]">اختر محادثة من القائمة أو اطلب وصلة جديدة بالكود.</p><button data-testid="button-empty-request" onClick={() => setShowRequest(true)} className="mt-7 flex h-12 items-center gap-2 rounded-xl bg-[#52e2c7] px-5 text-sm font-bold text-[#0e1727]"><Plus className="h-4 w-4" /> اطلب محادثة</button></div>)}
            </div>
          </div>
        </section>
      </div>
      {showRequest && <Modal title="اطلب محادثة" description="أدخل كود الشخص كما هو. سيقرر هو إن كانت الوصلة ستُفتح." onClose={() => setShowRequest(false)}><label htmlFor="target-code" className="mb-2 block text-sm font-semibold text-[#c3cedd]">كود الشخص</label><input id="target-code" data-testid="input-target-code" value={targetCode} onChange={(event) => setTargetCode(event.target.value.toUpperCase())} maxLength={12} placeholder="مثال: WS-7K4P" className="h-14 w-full rounded-2xl border border-[#2b3e58] bg-[#0d192b] px-4 font-display text-lg tracking-[.12em] text-[#e8edf7] outline-none placeholder:text-[#65758b] focus:border-[#52e2c7] focus:ring-4 focus:ring-[#52e2c7]/10" />{createRequest.isError && <p data-testid="status-request-error" className="mt-2 text-xs text-[#ffacb8]">تعذر إرسال الطلب. تحقق من الكود وحاول مرة أخرى.</p>}<button data-testid="button-submit-request" disabled={createRequest.isPending || targetCode.trim().length < 4} onClick={submitRequest} className="mt-4 flex h-13 w-full items-center justify-center gap-2 rounded-2xl bg-[#52e2c7] font-bold text-[#0e1727] disabled:opacity-40">{createRequest.isPending ? <LoaderCircle className="h-5 w-5 animate-spin" /> : <>إرسال الطلب <ArrowLeft className="h-5 w-5" /></>}</button></Modal>}
      {showGroup && <Modal title="مجموعة جديدة" description="اجمع أكثر من وصلة في مساحة واحدة. أضف الأكواد مفصولة بمسافة أو فاصلة." onClose={() => setShowGroup(false)}><label htmlFor="group-name" className="mb-2 block text-sm font-semibold text-[#c3cedd]">اسم المجموعة</label><input id="group-name" data-testid="input-group-name" value={groupName} onChange={(event) => setGroupName(event.target.value)} maxLength={80} placeholder="مثال: فريق الرحلة" className="mb-4 h-13 w-full rounded-2xl border border-[#2b3e58] bg-[#0d192b] px-4 text-sm text-[#e8edf7] outline-none placeholder:text-[#65758b] focus:border-[#52e2c7]" /><label htmlFor="member-codes" className="mb-2 block text-sm font-semibold text-[#c3cedd]">أكواد الأعضاء</label><input id="member-codes" data-testid="input-member-codes" value={memberCodes} onChange={(event) => setMemberCodes(event.target.value.toUpperCase())} placeholder="WS-7K4P، WS-2Q9M" className="h-13 w-full rounded-2xl border border-[#2b3e58] bg-[#0d192b] px-4 font-display text-sm tracking-wider text-[#e8edf7] outline-none placeholder:text-[#65758b] focus:border-[#52e2c7]" />{createGroup.isError && <p data-testid="status-group-error" className="mt-2 text-xs text-[#ffacb8]">تعذر إنشاء المجموعة. تحقق من الأكواد المدخلة.</p>}<button data-testid="button-submit-group" disabled={createGroup.isPending || !groupName.trim() || !memberCodes.trim()} onClick={submitGroup} className="mt-4 flex h-13 w-full items-center justify-center gap-2 rounded-2xl bg-[#52e2c7] font-bold text-[#0e1727] disabled:opacity-40">{createGroup.isPending ? <LoaderCircle className="h-5 w-5 animate-spin" /> : <>إنشاء المجموعة <Users className="h-5 w-5" /></>}</button></Modal>}
      {notice && <Notice text={notice} onClose={() => setNotice("")} />}
    </main>
  );
}