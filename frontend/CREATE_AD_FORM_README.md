# CreateAdForm - Komponenta pro vytváření inzerátů

Kompletní React komponenta pro vytváření inzerátů v e-shopu/marketplace systému s podporou uploadu více obrázků, validace a auto-save funkcionality.

## 📋 Funkce

### ✅ Implementované funkce

- **Kompletní formulář** se všemi požadovanými poli v češtině
- **Upload více obrázků** s drag & drop podporou
- **Preview obrázků** před odesláním
- **Nastavení hlavního obrázku** - označení jednoho obrázku jako hlavní
- **Smazání obrázků** - možnost odstranit jednotlivé obrázky
- **Přesouvání obrázků** - drag & drop pro změnu pořadí
- **Validace formuláře** - kompletní validace všech polí s chybovými hlášeními
- **Progress bar** - zobrazení průběhu nahrávání
- **Auto-save** - automatické ukládání do localStorage jako draft
- **Responzivní design** - optimalizováno pro mobil i desktop
- **Potvrzovací zpráva** - zobrazení úspěšného odeslání
- **Čištění formuláře** - po úspěšném odeslání

## 🎨 Design

Komponenta používá:
- **Tailwind CSS** pro moderní, čistý design
- **Card layout** s stínováním
- **Responzivní grid** pro obrázky (2 sloupce na mobilu, 4 na desktopu)
- **Hover efekty** na obrázcích
- **Color coding** - červené pro chyby, zelené pro úspěch, modré pro akce

## 📦 Struktura

```
frontend/src/
├── components/
│   └── CreateAdForm.jsx      # Hlavní komponenta
├── pages/
│   └── CreateAdPage.jsx      # Stránka s komponentou
└── services/
    └── adService.js          # API služba pro inzeráty
```

## 🚀 Použití

### Základní použití

```jsx
import CreateAdForm from './components/CreateAdForm';

function App() {
  return <CreateAdForm />;
}
```

### Přidání do routingu

Komponenta je již přidána do `App.jsx` na route `/ads/create`.

## 📝 Pole formuláře

### Povinná pole (označena červenou hvězdičkou *)

1. **Název inzerátu** (text)
   - Min. 5 znaků
   - Validace: required, minLength

2. **Popis** (textarea)
   - Min. 20 znaků
   - Validace: required, minLength

3. **Kategorie** (select)
   - Možnosti: Elektronika, Domácnost, Oblečení, Ostatní
   - Validace: required

4. **Cena** (number)
   - Pouze pro typ "Prodej"
   - Validace: > 0

5. **E-mail** (email)
   - Validace: required, email format

6. **Telefon** (tel)
   - Validace: required, phone format

7. **Lokalita** (text)
   - Validace: required

8. **Souhlas s podmínkami** (checkbox)
   - Validace: required

### Volitelná pole

1. **Měna** (select)
   - Výchozí: Kč
   - Možnosti: Kč, EUR

2. **Typ nabídky** (radio)
   - Výchozí: Prodej
   - Možnosti: Prodej, Darování, Výměna

3. **Obrázky** (file upload)
   - Více obrázků
   - Podporované formáty: JPG, PNG, GIF
   - Drag & drop podpora

## 🔧 API Integrace

Komponenta odesílá data na endpoint `/api/ads` pomocí POST požadavku s `multipart/form-data`.

### Formát dat

```javascript
{
  title: string,
  description: string,
  category: string,
  price: number,
  currency: string,
  offerType: string,
  email: string,
  phone: string,
  location: string,
  images: File[],
  mainImageIndex: number
}
```

### API Service

Komponenta používá `adService.js` pro komunikaci s backendem:

```javascript
import { createAd } from '../services/adService';

await createAd({
  ...formData,
  images: images,
  mainImageIndex: mainImageIndex,
  onProgress: (progress) => {
    setUploadProgress(progress);
  },
});
```

## 💾 Auto-save (Draft)

Formulář automaticky ukládá data do `localStorage` jako draft:

- **Ukládání**: Po 1 sekundě nečinnosti
- **Načítání**: Při načtení komponenty
- **Mazání**: Po úspěšném odeslání nebo ručním zrušení

### Formát draftu

```json
{
  "formData": {
    "title": "...",
    "description": "...",
    ...
  },
  "images": ["data:image/jpeg;base64,...", ...]
}
```

## 🖼️ Práce s obrázky

### Upload obrázků

- **Kliknutí**: Kliknutí na drag & drop oblast otevře file picker
- **Drag & Drop**: Přetažení obrázků do oblasti
- **Více obrázků**: Možnost nahrát více obrázků najednou

### Správa obrázků

- **Náhled**: Zobrazení thumbnails všech nahraných obrázků
- **Hlavní obrázek**: Označení jednoho obrázku jako hlavní (modrý rámeček)
- **Smazání**: Odstranění jednotlivých obrázků
- **Přesouvání**: Drag & drop pro změnu pořadí

### Zobrazení

- **Grid layout**: 2 sloupce na mobilu, 4 na desktopu
- **Hover efekty**: Zobrazení tlačítek při najetí myší
- **Vizuální indikace**: Hlavní obrázek má modrý rámeček a nálepku "Hlavní"

## ✅ Validace

### Validace polí

- **Název**: Required, min. 5 znaků
- **Popis**: Required, min. 20 znaků
- **Kategorie**: Required
- **Cena**: Required pro "Prodej", musí být > 0
- **E-mail**: Required, validní email format
- **Telefon**: Required, validní telefonní číslo
- **Lokalita**: Required
- **Souhlas**: Required

### Zobrazení chyb

- Chyby se zobrazují pod každým polem v červené barvě
- Chyba se automaticky vymaže při začátku psaní
- Před odesláním se provede kompletní validace

## 🎯 Stavy komponenty

### Loading state

- **isSubmitting**: Během odesílání formuláře
- **uploadProgress**: Procento nahrávání (0-100)
- **Progress bar**: Zobrazení průběhu nahrávání

### Success state

- **submitSuccess**: Po úspěšném odeslání
- **Zpráva**: "✅ Inzerát byl úspěšně vložen!"
- **Auto-reset**: Zpráva zmizí po 5 sekundách

### Error state

- **errors**: Objekt s chybami pro jednotlivá pole
- **errors.submit**: Obecná chyba při odesílání
- **Zobrazení**: Červené zprávy pod poli nebo v alert boxu

## 🎨 Styling

### Barvy

- **Primární**: Modrá (`bg-blue-600`)
- **Úspěch**: Zelená (`bg-green-50`)
- **Chyba**: Červená (`border-red-500`, `text-red-600`)
- **Neutrální**: Šedá (`bg-gray-50`, `border-gray-300`)

### Responzivní breakpointy

- **Mobile**: `< 768px` - 1 sloupec, menší padding
- **Desktop**: `>= 768px` - 2-4 sloupce, větší padding

### Komponenty

- **Card**: Bílá karta se stínem (`bg-white rounded-lg shadow-md`)
- **Input**: Zaoblené rohy, focus ring (`rounded-lg focus:ring-2`)
- **Button**: Primární modré tlačítko, disabled stav
- **Thumbnail**: Zaoblené rohy, hover efekty

## 🔍 Příklady použití

### Základní použití

```jsx
import CreateAdForm from './components/CreateAdForm';

function CreateAdPage() {
  return <CreateAdForm />;
}
```

### S vlastními handlery

```jsx
function CustomAdForm() {
  const handleSuccess = (data) => {
    console.log('Inzerát vytvořen:', data);
    // Přesměrování nebo další akce
  };

  return <CreateAdForm onSuccess={handleSuccess} />;
}
```

## 🐛 Řešení problémů

### Obrázky se nenačítají

- Zkontrolujte, zda jsou obrázky ve správném formátu (JPG, PNG, GIF)
- Zkontrolujte velikost souborů (doporučeno max 5MB na obrázek)

### Draft se nenačítá

- Zkontrolujte `localStorage` v DevTools
- Ověřte, zda není localStorage plný nebo zablokovaný

### Validace nefunguje

- Zkontrolujte, zda jsou všechna povinná pole vyplněna
- Ověřte formát e-mailu a telefonu

### API chyba

- Zkontrolujte, zda backend endpoint `/api/ads` existuje
- Ověřte CORS nastavení na backendu
- Zkontrolujte network tab v DevTools

## 📚 Další vylepšení

### Možná rozšíření

1. **Kategorie z API**: Načítání kategorií z backendu místo hardcoded
2. **Lokalita s autocomplete**: Integrace s mapovým API
3. **WYSIWYG editor**: Pro popis inzerátu
4. **Obrázková úprava**: Oříznutí, změna velikosti před uploadem
5. **Přehled inzerátů**: Seznam všech vytvořených inzerátů
6. **Úprava inzerátu**: Editace existujících inzerátů

## 📄 Licence

Tento komponenta je součástí e-shop projektu a je určena pro vzdělávací účely.

