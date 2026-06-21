# Project-Scoped Rules for money2

## Automated Google Play Release Notes (What's New)
When the user asks to "prepare a release", "create a new release", or "tag a new version":
1. Do NOT immediately tag and push.
2. First, analyze the recent Git commits (`git log` from the last tag to `HEAD`).
3. Summarize the technical commits into a highly polished, user-friendly "What's New" release note in Traditional Chinese (zh-TW). It should be suitable for general users to read on the Google Play Store.
4. Save this release note to the file: `play/release-notes/whatsnew-zh-TW` (overwrite it if it exists). Note: The filename must be exactly `whatsnew-zh-TW` without any file extension.
5. Create the directory `play/release-notes/` if it does not exist.
6. Commit the `play/release-notes/whatsnew-zh-TW` file to the repository.
7. Only after the commit is successful, create the Git tag (e.g., `v1.2.0`) and push both the commit and the tag to GitHub to trigger the release workflow.
