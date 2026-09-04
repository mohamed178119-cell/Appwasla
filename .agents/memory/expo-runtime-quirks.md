---
name: Expo runtime notes
description: Environment-specific Expo preview and generated-client behavior worth checking before debugging app code.
---

The Expo workflow can start and bundle the app even when the optional React Native DevTools binary reports a missing libglib shared library. Treat that message as a preview-tool warning unless Metro or the app itself fails.

**Why:** The preview environment may not include the native library used only by DevTools, while the Expo web/native bundle remains usable.

**How to apply:** Check the Metro bundle and application logs separately from the optional DevTools installation message. For this generated API client setup, provide an explicit queryKey when TypeScript requires it in a query options object.