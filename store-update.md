# Store Listing Updates — Steady Hand

Actions to take directly in App Store Connect and Google Play Console.
Code fixes that accompany these are on the `fable-fixes-md` branch.

---

## 1. App Name — ASO Priority (Both Stores)

**Current:**
- Google Play: "Steady Hand - Livestock Manager"
- App Store: "Steady Hand"

**Change to:**
- Google Play: "Steady Hand: Livestock Tracker"
- App Store Name: "Steady Hand: Livestock Tracker"
- App Store Subtitle: "Farm & Herd Record Keeping" *(30 char limit — this is 30)*

**Why:** "Steady Hand" alone has zero search volume. Target searches are "livestock tracker", "cattle records", "goat record keeping", "farm animal tracker", "herd management app". The subtitle is crawled heavily by the App Store algorithm — make every character count.

---

## 2. Keywords (App Store only — 100 chars max)

**Current:**
```
livestock,farm,cattle,goats,sheep,chickens,breeding,herd,flock,homestead,ranch,poultry,animals
```
*(94 chars — wastes space, repeats words already in name/subtitle)*

**Change to:**
```
calving,kidding,lambing,gestation,records,tracker,herd,veterinary,breeding,ranching,poultry
```
*(92 chars)*

**Notes:**
- Drop words already in the name/subtitle (livestock, farm, cattle, goats, sheep, chickens, animals, ranch, flock, homestead) — the algorithm already scores those from the name fields
- Add high-intent lifecycle terms ("calving", "kidding", "lambing", "gestation") — these are how real farmers search
- "veterinary" and "records" are evergreen search terms in this category

---

## 3. Short Description — Google Play (80 chars max)

**Current:**
```
Track livestock, breeding cycles, health records, and production for your farm.
```
*(80 chars — at limit)*

**Change to:**
```
Never miss a birth, vaccination, or breeding event on your farm.
```
*(64 chars — outcome-first, leaves room if needed)*

---

## 4. Full Description — Both Stores

### Opening paragraph (most important — above the fold)

**Current:**
> "Steady Hand is the complete livestock management app designed for homesteaders, hobby farmers, and small-scale ranchers. Track your entire herd or flock from birth to market with comprehensive record-keeping tools."

**Change to:**
> "Never miss a calving date, a vaccination due, or a breeding cycle again. Steady Hand keeps your entire herd organized — from the day an animal is born to the day it leaves your farm — so you can spend less time on paperwork and more time farming."

**Why:** Lead with what the farmer *stops worrying about*, not a feature list. The current opener is generic ("comprehensive record-keeping tools" is what every app says).

### Update closing paragraph (both stores — code fix also made in STORE_LISTING.md)

**Current:**
> "Whether you raise beef cattle, dairy goats, laying hens, or a diverse mix of livestock, **Homesteader** helps you stay organized…"

**Already fixed in STORE_LISTING.md** — now reads "Steady Hand". Copy the updated text from `STORE_LISTING.md` into both store consoles.

### Add a Premium section to the description

Add this block before the closing paragraph in both stores:

```
STEADY HAND FREE
- Track up to 20 animals
- Full event, reminder, and calendar features

STEADY HAND PREMIUM (one-time purchase)
- Unlimited animals
- Export reports to CSV
- Remove all ads
```

---

## 5. Promotional Text — App Store only (170 chars, not indexed but shown on product page)

**Current:**
> "Track your livestock, breeding cycles, health records, and production all in one app. Perfect for homesteaders, hobby farmers, and small-scale ranchers."
*(152 chars)*

**Change to:**
> "Never miss a birth date or vaccination again. Track unlimited animals, breeding cycles, and health records — all stored privately on your device."
*(146 chars)*

---

## 6. Screenshots

The current screenshots module generates store screenshots via Roborazzi. Once the premium paywall redesign is in (from the code branch), regenerate them:

```bash
./gradlew :screenshots:recordScreenshots
```

Update screenshots in both stores. Priority shots to refresh:
1. Dashboard with the new upgrade prompt card visible
2. Animal list showing the capacity banner (near the 20-animal limit)
3. Premium screen (🌾 icon, proper price, feature list with "Unlimited animals" at top)
4. More screen (emoji icons, "Upgrade to Premium" card with dynamic price)

---

## 7. Content Rating / Category

Verify category in both stores is **Productivity** (not Lifestyle or Utilities) — productivity ranks better for "record keeping" and "tracker" search terms in this niche.

---

## 8. What's New / Release Notes

Once the branch is merged and submitted, use this as the release notes:

```
v1.x — Premium Redesign

• Free tier: track up to 20 animals at no cost
• Premium upgrade: unlimited animals + CSV export + no ads (one-time purchase)
• Fixed: purchase and restore confirmation messages now display correctly
• Fixed: ad banner moved below content (no longer covers the top of the screen)
• Upgrade prompt now appears contextually when you approach your animal limit
• Store listing updated (prices, feature list, app name branding)
```

---

## Priority Order

| # | Task | Effort | Impact |
|---|------|--------|--------|
| 1 | Fix App Store keywords | 2 min | High — directly affects search rank |
| 2 | Update App Store subtitle | 1 min | High |
| 3 | Fix both closings ("Homesteader" → "Steady Hand") | 2 min | Medium — trust/brand |
| 4 | Replace opening paragraph | 5 min | Medium — conversion |
| 5 | Add Free/Premium section to description | 5 min | Medium — sets expectations |
| 6 | Update short description (Play) | 2 min | Medium |
| 7 | Refresh screenshots after code ships | 30 min | High — #1 conversion driver |
| 8 | Verify category | 1 min | Low-medium |
