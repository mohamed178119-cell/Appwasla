import React, { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator, Alert, FlatList, Modal, Pressable, RefreshControl,
  ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { KeyboardAvoidingView } from 'react-native-keyboard-controller';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Ionicons } from '@expo/vector-icons';
import { useColors } from '@/hooks/useColors';
import {
  useCreateChatRequest, useCreateGroup, useHealthCheck, useRegisterDevice,
  useRespondToChatRequest, useSendMessage, useSyncDevice, useUpdatePresence,
} from '@workspace/api-client-react';
import type { Chat, ChatRequest, Device } from '@workspace/api-client-react';

const DEVICE_KEY = 'wasla_device_id';
type Mode = 'home' | 'chat' | 'about';

export default function Home() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const [deviceId, setDeviceId] = useState<string | null>(null);
  const [device, setDevice] = useState<Device | null>(null);
  const [name, setName] = useState('');
  const [mode, setMode] = useState<Mode>('home');
  const [chat, setChat] = useState<Chat | null>(null);
  const [modal, setModal] = useState<'request' | 'group' | null>(null);
  const [code, setCode] = useState('');
  const [groupName, setGroupName] = useState('');
  const [members, setMembers] = useState('');
  const [copied, setCopied] = useState(false);
  const [message, setMessage] = useState('');
  const register = useRegisterDevice();
  const sync = useSyncDevice(deviceId ?? '', { query: { enabled: !!deviceId, refetchInterval: 10000 } });
  const presence = useUpdatePresence();
  const request = useCreateChatRequest();
  const createGroup = useCreateGroup();
  const respond = useRespondToChatRequest();
  const send = useSendMessage();
  const health = useHealthCheck();

  useEffect(() => { AsyncStorage.getItem(DEVICE_KEY).then(setDeviceId); }, []);
  useEffect(() => { if (sync.data) setDevice(sync.data.device); }, [sync.data]);
  const chats = sync.data?.chats ?? [];
  const incoming = (sync.data?.requests ?? []).filter((r) => r.toDeviceId === deviceId && r.status === 'pending');

  const registerNow = () => {
    if (!name.trim()) return;
    register.mutate({ data: { displayName: name.trim() } }, {
      onSuccess: async (result) => { await AsyncStorage.setItem(DEVICE_KEY, result.id); setDeviceId(result.id); setDevice(result); },
      onError: (e) => Alert.alert('تعذر التسجيل', String(e)),
    });
  };
  const copyCode = async () => {
    if (typeof navigator !== 'undefined' && navigator.clipboard) await navigator.clipboard.writeText(device?.code ?? '');
    setCopied(true); setTimeout(() => setCopied(false), 1800);
  };
  const submitRequest = () => {
    if (!deviceId || !code.trim()) return;
    request.mutate({ deviceId, data: { targetCode: code.trim() } }, { onSuccess: () => { setModal(null); setCode(''); sync.refetch(); }, onError: () => Alert.alert('تعذر إرسال الطلب', 'تحقق من الرمز وحاول مجدداً.') });
  };
  const submitGroup = () => {
    if (!deviceId || !groupName.trim()) return;
    const memberCodes = members.split(/[,\s]+/).map((x) => x.trim()).filter(Boolean);
    createGroup.mutate({ deviceId, data: { name: groupName.trim(), memberCodes } }, { onSuccess: (result) => { setModal(null); setGroupName(''); setMembers(''); setChat(result); setMode('chat'); sync.refetch(); }, onError: () => Alert.alert('تعذر إنشاء المجموعة', 'أضف رموزاً صحيحة وحاول مجدداً.') });
  };
  if (!deviceId && !device) return <View style={[styles.root, { backgroundColor: colors.background, paddingTop: insets.top + 36 }]}><View style={styles.brand}><Ionicons name="radio-outline" size={42} color={colors.primary} /><Text style={[styles.title, { color: colors.foreground }]}>وصلة</Text><Text style={[styles.subtitle, { color: colors.mutedForeground }]}>محادثات خاصة، برمز واحد ثابت</Text></View><TextInput testID="display-name-input" value={name} onChangeText={setName} placeholder="اسم العرض" placeholderTextColor={colors.mutedForeground} style={[styles.input, { color: colors.foreground, borderColor: colors.border, backgroundColor: colors.card }]} /><Button testID="register-button" label="ابدأ الآن" icon="arrow-forward" onPress={registerNow} colors={colors} disabled={register.isPending} /><Text style={[styles.credit, { color: colors.mutedForeground }]}>وصلة — من تطوير Mohamed Saad</Text></View>;
  if (mode === 'chat' && chat) return <ChatView chat={chat} deviceId={deviceId!} message={message} setMessage={setMessage} onBack={() => { setMode('home'); setChat(null); }} onSend={() => { if (message.trim()) send.mutate({ chatId: chat.id, data: { deviceId: deviceId!, text: message.trim() } }, { onSuccess: () => { setMessage(''); sync.refetch(); } }); }} colors={colors} insets={insets} />;
  return <View style={[styles.root, { backgroundColor: colors.background, paddingTop: insets.top }]}><ScrollView refreshControl={<RefreshControl refreshing={sync.isFetching} onRefresh={() => sync.refetch()} tintColor={colors.primary} />} contentContainerStyle={{ padding: 20, paddingBottom: insets.bottom + 30 }}>
    <View style={styles.header}><View><Text style={[styles.kicker, { color: colors.primary }]}>وصلة</Text><Text style={[styles.heading, { color: colors.foreground }]}>أهلاً، {device?.displayName}</Text></View><Pressable testID="about-button" onPress={() => setMode('about')}><Ionicons name="information-circle-outline" size={28} color={colors.mutedForeground} /></Pressable></View>
    <View style={[styles.codeCard, { backgroundColor: colors.card, borderColor: colors.border }]}><Text style={[styles.label, { color: colors.mutedForeground }]}>رمز جهازك</Text><Text selectable style={[styles.code, { color: colors.foreground }]}>{device?.code}</Text><Pressable testID="copy-code-button" onPress={copyCode} style={styles.copy}><Ionicons name={copied ? 'checkmark' : 'copy-outline'} size={18} color={colors.primary} /><Text style={{ color: colors.primary }}>{copied ? 'تم النسخ' : 'نسخ الرمز'}</Text></Pressable></View>
    <View style={styles.row}><Text style={[styles.sectionTitle, { color: colors.foreground }]}>متصل الآن</Text><Pressable testID="presence-toggle" onPress={() => presence.mutate({ deviceId: deviceId!, data: { online: !device?.online } }, { onSuccess: (d) => setDevice(d) })} style={[styles.switch, { backgroundColor: device?.online ? colors.primary : colors.muted }]}><View style={[styles.knob, { backgroundColor: colors.foreground, alignSelf: device?.online ? 'flex-end' : 'flex-start' }]} /></Pressable></View>
    <View style={styles.actions}><Button testID="new-request-button" label="محادثة جديدة" icon="person-add-outline" onPress={() => setModal('request')} colors={colors} /><Button testID="new-group-button" label="مجموعة" icon="people-outline" onPress={() => setModal('group')} colors={colors} secondary /></View>
    <Text style={[styles.sectionTitle, { color: colors.foreground, marginTop: 24 }]}>المحادثات</Text>
    {sync.isLoading ? <ActivityIndicator color={colors.primary} style={{ margin: 30 }} /> : sync.isError ? <Empty text="تعذر تحميل المحادثات" action="إعادة المحاولة" onPress={() => sync.refetch()} colors={colors} /> : chats.length === 0 ? <Empty text="لا توجد محادثات بعد" action="ابدأ محادثة برمز" onPress={() => setModal('request')} colors={colors} /> : chats.map((item) => <Pressable testID={`chat-${item.id}`} key={item.id} onPress={() => { setChat(item); setMode('chat'); }} style={[styles.chatRow, { borderBottomColor: colors.border }]}><View style={[styles.avatar, { backgroundColor: colors.secondary }]}><Text style={{ color: colors.secondaryForeground, fontWeight: '700' }}>{item.name.slice(0, 1)}</Text></View><View style={{ flex: 1 }}><Text style={[styles.chatName, { color: colors.foreground }]}>{item.name}</Text><Text numberOfLines={1} style={{ color: colors.mutedForeground }}>{item.messages.at(-1)?.text ?? 'ابدأ المحادثة'}</Text></View><Ionicons name="chevron-back" size={18} color={colors.mutedForeground} /></Pressable>)}
    {incoming.length > 0 && <><Text style={[styles.sectionTitle, { color: colors.foreground, marginTop: 24 }]}>طلبات واردة</Text>{incoming.map((r) => <RequestRow key={r.id} request={r} deviceId={deviceId!} respond={respond} colors={colors} onDone={() => sync.refetch()} />)}</>}
  </ScrollView>{modal && <FormModal kind={modal} onClose={() => setModal(null)} code={code} setCode={setCode} groupName={groupName} setGroupName={setGroupName} members={members} setMembers={setMembers} onSubmit={modal === 'request' ? submitRequest : submitGroup} colors={colors} />}</View>;
}

function ChatView({ chat, deviceId, message, setMessage, onBack, onSend, colors, insets }: any) {
  const messages = useMemo(() => [...chat.messages].reverse(), [chat.messages]);
  return <KeyboardAvoidingView style={{ flex: 1, backgroundColor: colors.background }} behavior="padding"><View style={[styles.chatHeader, { paddingTop: insets.top + 8, borderBottomColor: colors.border }]}><Pressable testID="back-button" onPress={onBack}><Ionicons name="arrow-forward" size={26} color={colors.foreground} /></Pressable><Text style={[styles.chatName, { color: colors.foreground }]}>{chat.name}</Text></View><FlatList inverted data={messages} keyExtractor={(m: any) => m.id} renderItem={({ item }: any) => <View style={[styles.bubble, { backgroundColor: item.senderId === deviceId ? colors.primary : colors.card, alignSelf: item.senderId === deviceId ? 'flex-start' : 'flex-end' }]}><Text style={{ color: item.senderId === deviceId ? colors.primaryForeground : colors.foreground }}>{item.text}</Text></View>} contentContainerStyle={{ padding: 16, gap: 8 }} keyboardDismissMode="interactive" keyboardShouldPersistTaps="handled" /><View style={[styles.composer, { paddingBottom: insets.bottom + 8, borderTopColor: colors.border }]}><TextInput testID="message-input" value={message} onChangeText={setMessage} placeholder="اكتب رسالة..." placeholderTextColor={colors.mutedForeground} style={[styles.messageInput, { color: colors.foreground, backgroundColor: colors.card }]} /><Pressable testID="send-message-button" onPress={onSend} style={[styles.send, { backgroundColor: colors.primary }]}><Ionicons name="send" size={20} color={colors.primaryForeground} /></Pressable></View></KeyboardAvoidingView>;
}
function Button({ label, icon, onPress, colors, secondary, disabled, testID }: any) { return <Pressable testID={testID} disabled={disabled} onPress={onPress} style={({ pressed }) => [styles.button, { backgroundColor: secondary ? colors.muted : colors.primary, opacity: pressed || disabled ? 0.65 : 1 }]}><Ionicons name={icon} size={19} color={secondary ? colors.foreground : colors.primaryForeground} /><Text style={{ color: secondary ? colors.foreground : colors.primaryForeground, fontWeight: '700' }}>{label}</Text></Pressable>; }
function Empty({ text, action, onPress, colors }: any) { return <View style={styles.empty}><Ionicons name="chatbubble-ellipses-outline" size={34} color={colors.mutedForeground} /><Text style={{ color: colors.mutedForeground, marginVertical: 8 }}>{text}</Text><Pressable testID="retry-button" onPress={onPress}><Text style={{ color: colors.primary }}>{action}</Text></Pressable></View>; }
function RequestRow({ request, deviceId, respond, colors, onDone }: any) { return <View style={[styles.request, { backgroundColor: colors.card, borderColor: colors.border }]}><View style={{ flex: 1 }}><Text style={{ color: colors.foreground }}>طلب من الرمز {request.fromCode}</Text><Text style={{ color: colors.mutedForeground }}>يريد بدء محادثة معك</Text></View><Pressable testID={`reject-${request.id}`} onPress={() => respond.mutate({ requestId: request.id, data: { deviceId, status: 'rejected' } }, { onSuccess: onDone })}><Ionicons name="close-circle-outline" size={28} color={colors.destructive} /></Pressable><Pressable testID={`accept-${request.id}`} onPress={() => respond.mutate({ requestId: request.id, data: { deviceId, status: 'accepted' } }, { onSuccess: onDone })}><Ionicons name="checkmark-circle-outline" size={28} color={colors.primary} /></Pressable></View>; }
function FormModal({ kind, onClose, code, setCode, groupName, setGroupName, members, setMembers, onSubmit, colors }: any) { return <Modal transparent animationType="slide" visible onRequestClose={onClose}><View style={styles.overlay}><View style={[styles.sheet, { backgroundColor: colors.card }]}><Pressable testID="close-modal-button" onPress={onClose} style={styles.close}><Ionicons name="close" size={24} color={colors.foreground} /></Pressable><Text style={[styles.heading, { color: colors.foreground }]}>{kind === 'request' ? 'محادثة جديدة' : 'إنشاء مجموعة'}</Text>{kind === 'request' ? <TextInput testID="target-code-input" value={code} onChangeText={setCode} placeholder="رمز الجهاز" placeholderTextColor={colors.mutedForeground} style={[styles.input, { color: colors.foreground, borderColor: colors.border, backgroundColor: colors.background }]} /> : <><TextInput testID="group-name-input" value={groupName} onChangeText={setGroupName} placeholder="اسم المجموعة" placeholderTextColor={colors.mutedForeground} style={[styles.input, { color: colors.foreground, borderColor: colors.border, backgroundColor: colors.background }]} /><TextInput testID="member-codes-input" value={members} onChangeText={setMembers} placeholder="رموز الأعضاء، مفصولة بفاصلة أو مسافة" placeholderTextColor={colors.mutedForeground} style={[styles.input, { color: colors.foreground, borderColor: colors.border, backgroundColor: colors.background }]} /></>}<Button testID="submit-form-button" label="تأكيد" icon="checkmark" onPress={onSubmit} colors={colors} /></View></View></Modal>; }