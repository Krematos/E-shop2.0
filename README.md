# E-Shop React Frontend

Kompletní React frontend pro e-commerce projekt připojený k Spring Boot backend REST API.

## 🚀 Technologie

- **React 19** - Funkční komponenty a hooks
- **React Router v6** - Routing
- **Axios** - HTTP komunikace s API
- **Tailwind CSS** - Styling (moderní a čistý design)
- **React Context** - Správa globálního stavu (autentizace, košík, cookies)
- **Vite** - Build tool
- **Lucide React** - Ikony
- **Stripe Elements** - Platby platební kartou

## 📁 Struktura projektu

```
frontend/
├── public/                 # Statické soubory (sitemap.xml, robots.txt, favicon, obrázky)
├── src/
│   ├── components/        # Znovupoužitelné komponenty
│   │   ├── Header.jsx     # Hlavička s navigací
│   │   ├── Footer.jsx     # Patička s identifikačními údaji provozovatele (Tomáš Ryvola)
│   │   ├── SEO.jsx        # Správa meta tagů, Open Graph a Schema.org Rich Snippets
│   │   ├── ProductCard.jsx # Karta produktu s cenou včetně DPH
│   │   ├── CookieConsentBanner.jsx # Cookie lišta s možností odvolání
│   │   ├── LoadingSpinner.jsx # Indikátor načítání
│   │   └── ProtectedRoute.jsx # Chráněné routy
│   ├── context/           # React Context providers
│   │   ├── AuthContext.jsx # Správa autentizace
│   │   ├── CartContext.jsx  # Správa košíku
│   │   └── CookieConsentContext.jsx # Správa souhlasu s cookies
│   ├── pages/             # Stránky aplikace
│   │   ├── HomePage.jsx   # Domovská stránka
│   │   ├── ProductListPage.jsx # Seznam produktů
│   │   ├── ProductDetailPage.jsx # Detail produktu s cenou včetně DPH a SEO
│   │   ├── CartPage.jsx   # Košík s rekapitulací včetně DPH
│   │   ├── CheckoutPage.jsx # Pokladna („Objednávka zavazující k platbě“)
│   │   ├── CheckoutPaymentPage.jsx # Stripe platba kartou
│   │   ├── CheckoutConfirmPage.jsx # Potvrzení objednávky
│   │   ├── TermsPage.jsx  # Všeobecné obchodní podmínky
│   │   ├── PrivacyPage.jsx # Zásady ochrany osobních údajů (GDPR)
│   │   ├── LoginPage.jsx   # Přihlášení
│   │   ├── RegisterPage.jsx # Registrace
│   │   ├── ProfilePage.jsx # Profil uživatele
│   │   ├── AdminPage.jsx  # Administrace produktů
│   │   └── AdminOrdersPage.jsx # Administrace objednávek
│   ├── services/          # API služby
│   │   ├── api.js         # Axios konfigurace
│   │   ├── authService.js # Autentizační služby
│   │   ├── productService.js # Služby pro produkty
│   │   ├── orderService.js # Služby pro objednávky
│   │   └── userService.js # Služby pro uživatele
│   ├── utils/             # Pomocné funkce (urlUtils.js pro srcset obrázků)
│   ├── App.jsx            # Hlavní komponenta
│   ├── main.jsx           # Vstupní bod
│   └── index.css          # Globální styly (Tailwind)
├── .env                   # Environment proměnné (není v GIT)
├── .env.example           # Příklad .env souboru
├── package.json           # Závislosti
└── tailwind.config.js     # Konfigurace Tailwind
```
