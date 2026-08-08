# 📤 Uploading These Pages to the GitHub Wiki

The GitHub wiki lives in a separate git repository with the `.wiki.git` suffix.

## Steps

### 1. Create the wiki on GitHub

1. Go to https://github.com/jbstoker/DadTreasury
2. Click the **Wiki** tab
3. GitHub will ask you to create the first page — just save a blank `/Home` page (or copy `Home.md`)

### 2. Clone the wiki repo

```bash
git clone https://github.com/jbstoker/DadTreasury.wiki.git
cd DadTreasury.wiki
```

### 3. Copy the wiki-ready files

From the repo, copy the contents of `docs/wiki/` into the wiki clone:

```bash
cp docs/wiki/Home.md .

# Rename _Sidebar.md correctly
cp docs/wiki/_Sidebar.md _Sidebar.md

cp docs/wiki/Getting-Started.md Getting-Started.md
cp docs/wiki/Tasks.md Tasks.md
cp docs/wiki/Wallet-Time-Bank.md Wallet-Time-Bank.md
cp docs/wiki/Calendar.md Calendar.md
cp docs/wiki/Chat.md Chat.md
cp docs/wiki/Library.md Library.md
cp docs/wiki/Location-Rules.md Location-Rules.md
cp docs/wiki/Connect-Parent-Apps.md Connect-Parent-Apps.md
cp docs/wiki/Google-Family-Link.md Google-Family-Link.md
cp docs/wiki/Security.md Security.md
cp docs/wiki/Settings.md Settings.md
cp docs/wiki/Diagnostics.md Diagnostics.md
cp docs/wiki/Changelog.md Changelog.md
```

### 4. Commit and push

```bash
git add .
git commit -m "Add Dad's Treasury documentation"
git push
```

That's it! The wiki will now be live at:
```
https://github.com/jbstoker/DadTreasury/wiki
```

---

> 💡 **Tip:** GitHub wiki page names use dashes for spaces, e.g. `Getting-Started.md` becomes "Getting Started" in the wiki.