# E-Shop React Frontend

Kompletní React frontend pro e-commerce projekt připojený k Spring Boot backend REST API.

## 🚀 Technologie

- **React 18+** - Funkční komponenty a hooks
- **React Router v6** - Routing
- **Axios** - HTTP komunikace s API
- **Tailwind CSS** - Styling (moderní a čistý design)
- **React Context** - Správa globálního stavu (autentizace, košík)
- **Vite** - Build tool

## 📁 Struktura projektu

```
frontend/
├── public/                 # Statické soubory
├── src/
│   ├── components/        # Znovupoužitelné komponenty
│   │   ├── Header.jsx     # Hlavička s navigací
│   │   ├── Footer.jsx     # Patička
│   │   ├── ProductCard.jsx # Karta produktu
│   │   ├── LoadingSpinner.jsx # Indikátor načítání
│   │   └── ProtectedRoute.jsx # Chráněné routy
│   ├── context/           # React Context providers
│   │   ├── AuthContext.jsx # Správa autentizace
│   │   └── CartContext.jsx  # Správa košíku
│   ├── pages/             # Stránky aplikace
│   │   ├── HomePage.jsx   # Domovská stránka
│   │   ├── ProductListPage.jsx # Seznam produktů
│   │   ├── ProductDetailPage.jsx # Detail produktu
│   │   ├── CartPage.jsx   # Košík
│   │   ├── LoginPage.jsx   # Přihlášení
│   │   ├── RegisterPage.jsx # Registrace
│   │   ├── ProfilePage.jsx # Profil uživatele
│   │   └── AdminPage.jsx  # Administrace
│   ├── services/          # API služby
│   │   ├── api.js         # Axios konfigurace
│   │   ├── authService.js # Autentizační služby
│   │   ├── productService.js # Služby pro produkty
│   │   ├── orderService.js # Služby pro objednávky
│   │   └── userService.js # Služby pro uživatele
│   ├── App.jsx            # Hlavní komponenta
│   ├── main.jsx           # Vstupní bod
│   └── index.css          # Globální styly (Tailwind)
├── .env                   # Environment proměnné (není v GIT)
├── .env.example           # Příklad .env souboru
├── package.json           # Závislosti
├── tailwind.config.js     # Konfigurace Tailwind
├── postcss.config.js       # Konfigurace PostCSS
└── vite.config.js         # Konfigurace Vite
```

## 📋 Stránky a funkce

### Domovská stránka (`/`)
- Zobrazení doporučených produktů
- Vyhledávací lišta
- Kategorie produktů

### Seznam produktů (`/products`)
- Zobrazení všech produktů z backendu (`/api/products`)
- Vyhledávání produktů
- Filtrování podle kategorií
- Tlačítko "Přidat do košíku" u každého produktu

### Detail produktu (`/products/:id`)
- Zobrazení detailu produktu z `/api/products/{id}`
- Obrázek, popis, cena
- Tlačítko "Přidat do košíku" s možností výběru množství

### Košík (`/cart`)
- Zobrazení přidaných produktů
- Úprava množství
- Odstranění produktů
- Checkout přes `/api/orders`

### Přihlášení (`/login`)
- Připojení k `/api/auth/login`
- Uložení JWT tokenu do localStorage
- Automatické přesměrování po přihlášení

### Registrace (`/register`)
- Připojení k `/api/auth/register`
- Validace formuláře
- Přesměrování na přihlášení po úspěšné registraci

### Profil uživatele (`/profile`)
- Informace o uživateli z localStorage
- Historie objednávek z `/api/orders` (objednávky přihlášeného uživatele)
- Chráněná stránka (vyžaduje přihlášení)

### Administrace (`/admin`)
- Přístup pouze pro uživatele s rolí ADMIN
- **Správa produktů:**
  - Zobrazení všech produktů
  - Přidání nového produktu (`POST /api/products`)
  - Úprava produktu (`PUT /api/products/{id}`)
  - Smazání produktu (`DELETE /api/products/{id}`)
- **Zobrazení objednávek:**
  - Seznam všech objednávek z `/api/orders/all`

## 🔧 Instalace a spuštění

### Požadavky
- Node.js 18+ a npm

### Kroky

1. **Instalace závislostí:**
   ```bash
   cd frontend
   npm install
   ```

2. **Konfigurace environment proměnných:**
   
   Vytvořte soubor `.env` v adresáři `frontend/` s následujícím obsahem:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```
   
   Poznámka: Upravte URL podle vašeho backendu, pokud běží na jiném portu.

3. **Spuštění vývojového serveru:**
   ```bash
   npm run dev
   ```
   
   Aplikace bude dostupná na `http://localhost:5173` (nebo jiném portu, který Vite přiřadí).

4. **Sestavení pro produkci:**
   ```bash
   npm run build
   ```
   
   Sestavené soubory budou v adresáři `dist/`.

5. **Náhled produkční verze:**
   ```bash
   npm run preview
   ```

## 🔐 Autentizace a JWT

Aplikace automaticky:
- Přidává JWT token do hlavičky `Authorization: Bearer <token>` u všech API požadavků
- Ukládá token do `localStorage` po přihlášení
- Odstraňuje token při odhlášení
- Přesměrovává na `/login` při 401 chybě (neplatný/vypršený token)

## 🛒 Správa košíku

Košík je spravován pomocí React Context a ukládá se do `localStorage`:
- Produkty zůstávají v košíku i po obnovení stránky
- Množství lze upravovat
- Při checkoutu se vytvoří objednávka přes API

## 📱 Responzivní design

Aplikace je plně responzivní a optimalizovaná pro:
- Mobilní zařízení
- Tablety
- Desktop

## 🌐 API Endpointy

Frontend komunikuje s následujícími backend endpointy:

- `POST /api/auth/register` - Registrace
- `POST /api/auth/login` - Přihlášení
- `GET /api/auth/validate` - Ověření tokenu
- `GET /api/products` - Seznam produktů
- `GET /api/products/{id}` - Detail produktu
- `POST /api/products` - Vytvoření produktu (ADMIN)
- `PUT /api/products/{id}` - Úprava produktu (ADMIN)
- `DELETE /api/products/{id}` - Smazání produktu (ADMIN)
- `POST /api/orders` - Vytvoření objednávky
- `GET /api/orders` - Objednávky uživatele
- `GET /api/orders/all` - Všechny objednávky (ADMIN)
- `GET /api/user/{id}` - Informace o uživateli

## 🎨 Styling

Aplikace používá Tailwind CSS s vlastními utility třídami:
- `.btn-primary` - Primární tlačítko
- `.btn-secondary` - Sekundární tlačítko
- `.btn-danger` - Nebezpečné akce (smazat)
- `.input-field` - Vstupní pole
- `.card` - Karta/kontejner

## 📝 Poznámky

- Všechny texty a UI elementy jsou v češtině
- Backend musí běžet na adrese uvedené v `.env`
- CORS musí být správně nakonfigurován na backendu
- JWT token se automaticky přidává do všech požadavků

## 🐛 Řešení problémů

**Aplikace se nespustí:**
- Zkontrolujte, zda máte nainstalované všechny závislosti (`npm install`)
- Zkontrolujte verzi Node.js (měla by být 18+)

**API požadavky selhávají:**
- Zkontrolujte, zda backend běží
- Ověřte `VITE_API_BASE_URL` v `.env` souboru
- Zkontrolujte CORS nastavení na backendu

**Token se neukládá:**
- Zkontrolujte konzoli prohlížeče pro chyby
- Ověřte, zda backend vrací token v odpovědi na `/api/auth/login`

## 📄 Licence

Tento projekt je vytvořen pro vzdělávací účely.
