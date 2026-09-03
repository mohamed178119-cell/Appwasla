# وصلة — محادثات بالأكواد

تطبيق محادثات عربي يمنح كل تثبيت كودًا ثابتًا للتواصل بين الأجهزة عبر خادم مركزي وقاعدة بيانات.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server (port 5000)
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

## Where things live

- `artifacts/wasla` — واجهة وصلة العربية RTL وتجربة المحادثات.
- `artifacts/api-server/src/routes/wasla.ts` — مسارات التسجيل، التزامن، الطلبات، المجموعات والرسائل.
- `lib/db/src/schema/wasla.ts` — جداول PostgreSQL الخاصة بالأجهزة والمحادثات والأعضاء والطلبات والرسائل.
- `lib/api-spec/openapi.yaml` — العقد المصدر الوحيد لمسارات API؛ شغّل codegen بعد أي تعديل.

## Architecture decisions

- كل تثبيت يسجل جهازًا مرة واحدة ويحتفظ التطبيق بـ `deviceId` محليًا، بينما الكود الثابت يُنشأ ويُحفظ على الخادم.
- لا توجد حسابات بريد أو كلمات مرور في النسخة الحالية؛ صلاحية التواصل تعتمد على معرفة الكود وقبول الطرف الآخر.
- التزامن الحالي يتم عبر إعادة جلب حالة الجهاز دوريًا، ما يسمح لنسختين مستقلتين بالتواصل دون الاعتماد على اتصال WebSocket.
- قبول الطلب يتم من الجهاز المستهدف نفسه، وتُحفظ حالة العضوية لكل جهاز حتى تختلف حالة الطلب بين الطرفين.

## Product

- تسجيل نسخة وصلة باسم ظاهر وكود ثابت.
- نسخ الكود ومشاركة طلب محادثة مع نسخة أخرى.
- قبول أو تجاهل طلبات المحادثة، وإنشاء مجموعات بالأكواد.
- إرسال رسائل محفوظة على الخادم مع ظهور حالة الاتصال والتزامن بين الأجهزة.
- واجهة عربية RTL متجاوبة تشمل المحادثات والطلبات وصفحة عن التطبيق.

## User preferences

_Populate as you build — explicit user instructions worth remembering across sessions._

## Gotchas

_Populate as you build — sharp edges, "always run X before Y" rules._

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
