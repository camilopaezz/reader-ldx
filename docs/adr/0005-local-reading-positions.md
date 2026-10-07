# Keep reading positions local to each device

Reading positions are saved and restored independently on each device rather than synchronized through the server. We chose per-device resumption over automatic cross-device handoff to remove divergent-position conflicts from the sync model. Books, highlights, bookmarks, annotation notes, dictionaries, and reading settings still sync; a bookmark can mark a passage across devices without becoming an automatically synced reading position.
