# share-callback-demo
Demo of how to use standard system dialogs to share content and get a callback (e.g., for tracking)

Note: the chooser is what you should try to use, but it's only available on API 22 (Android 5.1) and later, so you 
should fallback to an intent picker on previous APIs (or, a totally custom share dialog that looks like a Material
Design intent chooser).
