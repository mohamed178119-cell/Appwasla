FROM node:22-slim AS base
RUN corepack enable

FROM base AS builder
WORKDIR /app
COPY . .
RUN pnpm install --frozen-lockfile
RUN pnpm --filter @workspace/api-server run build

FROM base AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/artifacts/api-server/dist ./dist
EXPOSE 8080
ENV PORT=8080
CMD ["node", "--enable-source-maps", "dist/index.mjs"]
