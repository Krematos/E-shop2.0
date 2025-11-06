# Struktura projektu - E-Shop Frontend

## 📂 Přehled adresářů

```
frontend/
│
├── public/                          # Statické soubory
│   └── vite.svg
│
├── src/
│   ├── components/                   # Znovupoužitelné komponenty
│   │   ├── Header.jsx               # Hlavička s navigací a košíkem
│   │   ├── Footer.jsx               # Patička stránky
│   │   ├── ProductCard.jsx          # Karta produktu pro zobrazení v seznamech
│   │   ├── LoadingSpinner.jsx       # Komponenta pro zobrazení načítání
│   │   └── ProtectedRoute.jsx       # HOC pro chráněné routy
│   │
│   ├── context/                      # React Context providers
│   │   ├── AuthContext.jsx          # Správa autentizace a uživatelského stavu
│   │   └── CartContext.jsx          # Správa košíku a jeho stavu
│   │
│   ├── pages/                        # Stránky aplikace
│   │   ├── HomePage.jsx             # Domovská stránka s vyhledáváním
│   │   ├── ProductListPage.jsx      # Seznam všech produktů
│   │   ├── ProductDetailPage.jsx    # Detail konkrétního produktu
│   │   ├── CartPage.jsx             # Košík s možností checkoutu
│   │   ├── LoginPage.jsx            # Přihlašovací formulář
│   │   ├── RegisterPage.jsx         # Registrační formulář
│   │   ├── ProfilePage.jsx          # Profil uživatele s historií objednávek
│   │   └── AdminPage.jsx            # Administrační panel (pouze ADMIN)
│   │
│   ├── services/                     # API služby
│   │   ├── api.js                   # Axios instance s interceptory
│   │   ├── authService.js           # Autentizační služby (login, register)
│   │   ├── productService.js        # Služby pro práci s produkty
│   │   ├── orderService.js          # Služby pro práci s objednávkami
│   │   └── userService.js           # Služby pro práci s uživateli
│   │
│   ├── App.jsx                       # Hlavní komponenta s routingem
│   ├── main.jsx                      # Vstupní bod aplikace
│   └── index.css                     # Globální styly (Tailwind CSS)
│
├── .env                              # Environment proměnné (není v GIT)
├── .env.example                      # Příklad .env souboru
├── ENV_EXAMPLE.txt                   # Alternativní příklad .env
├── package.json                      # NPM závislosti a skripty
├── tailwind.config.js                # Konfigurace Tailwind CSS
├── postcss.config.js                 # Konfigurace PostCSS
├── vite.config.js                    # Konfigurace Vite
└── README.md                         # Dokumentace projektu
```

## 🔄 Tok dat

1. **Autentizace:**
   - Uživatel se přihlásí → `authService.login()` → JWT token uložen do localStorage
   - Token se automaticky přidává do všech API požadavků přes Axios interceptor
   - `AuthContext` spravuje stav přihlášeného uživatele

2. **Košík:**
   - Produkty se přidávají do košíku → `CartContext.addToCart()`
   - Košík se ukládá do localStorage
   - Při checkoutu se vytvoří objednávka přes `orderService.createOrder()`

3. **Produkty:**
   - Načítání produktů → `productService.getProducts()`
   - Detail produktu → `productService.getProductById()`
   - Admin operace → `productService.create/update/deleteProduct()`

## 🛣️ Routing

- `/` - Domovská stránka
- `/products` - Seznam produktů
- `/products/:id` - Detail produktu
- `/cart` - Košík
- `/login` - Přihlášení
- `/register` - Registrace
- `/profile` - Profil (chráněno - vyžaduje přihlášení)
- `/admin` - Administrace (chráněno - vyžaduje roli ADMIN)

## 🔐 Bezpečnost

- JWT token se automaticky přidává do hlavičky `Authorization: Bearer <token>`
- Chráněné routy kontrolují autentizaci a role
- Token se validuje při startu aplikace
- Při 401 chybě se uživatel automaticky odhlásí a přesměruje na `/login`

## 📦 State Management

- **AuthContext:** Uživatelská autentizace, role, přihlášení/odhlášení
- **CartContext:** Produkty v košíku, množství, celková cena

## 🎨 Styling

- Tailwind CSS pro utility-first styling
- Vlastní utility třídy v `index.css`:
  - `.btn-primary`, `.btn-secondary`, `.btn-danger`
  - `.input-field`
  - `.card`

## 🌐 API Komunikace

Všechny API volání procházejí přes:
- `services/api.js` - Axios instance s base URL z `.env`
- Automatické přidání JWT tokenu
- Automatické zpracování 401 chyb

