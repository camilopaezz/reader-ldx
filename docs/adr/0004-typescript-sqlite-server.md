# Use TypeScript and SQLite for the personal sync server

The user chose TypeScript for the self-hosted sync service, explicitly rejecting a Kotlin server and preferring TypeScript over the proposed Go alternative. Use Node LTS, Fastify, and better-sqlite3, deployed as a container with persistent storage, avoiding a required separate database service for this personal deployment. The user expects to put it behind a reverse proxy; sync uses a server-managed username/password login.
