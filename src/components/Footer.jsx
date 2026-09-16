import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-white mt-auto">
      <div className="container mx-auto px-4 py-10">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* O e-shopu */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-white">SecondEL</h3>
            <p className="text-gray-400 text-sm leading-relaxed">
              Váš spolehlivý e-shop pro nákup prověřené elektroniky se zárukou a rychlým dodáním.
            </p>
            <div className="mt-4">
              <Link to="/about" className="text-sm text-primary-400 hover:text-primary-300 transition-colors inline-flex items-center gap-1">
                Více informací o nás &rarr;
              </Link>
            </div>
          </div>

          {/* Identifikační údaje provozovatele */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-white">Provozovatel e-shopu</h3>
            <div className="text-sm text-gray-400 space-y-1">
              <p className="text-white font-medium">Tomáš Ryvola</p>
              <p>Křižíkova 265</p>
              <p>340 34 Plánice</p>
              <p className="pt-1"><span className="text-gray-300 font-medium">IČO:</span> 23079100</p>
              <p className="text-xs text-gray-400 pt-1 leading-normal">
                Fyzická osoba podnikající dle živnostenského zákona nezapsaná v obchodním rejstříku.
              </p>
            </div>
          </div>

          {/* Kontakt */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-white">Kontakt & Podpora</h3>
            <div className="text-sm text-gray-400 space-y-2">
              <p>
                <span className="text-gray-300 font-medium">E-mail:</span>{' '}
                <a href="mailto:info@secondel.cz" className="hover:text-white transition-colors">
                  info@secondel.cz
                </a>
              </p>
              <p>
                <span className="text-gray-300 font-medium">Telefon:</span>{' '}
                <a href="tel:+420123456789" className="hover:text-white transition-colors">
                  +420 123 456 789
                </a>
              </p>
              <p className="text-xs text-gray-400">
                Po–Pá: 9:00 – 17:00
              </p>
            </div>
          </div>

          {/* Právní odkazy */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-white">Důležité informace</h3>
            <ul className="space-y-2 text-sm text-gray-400">
              <li>
                <Link to="/terms" className="hover:text-white transition-colors">
                  Obchodní podmínky
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="hover:text-white transition-colors">
                  Zásady ochrany osobních údajů (GDPR)
                </Link>
              </li>
              <li>
                <Link to="/products" className="hover:text-white transition-colors">
                  Katalog produktů
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Spodní lišta */}
        <div className="border-t border-gray-800 mt-8 pt-6 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-4">
          <p>&copy; {new Date().getFullYear()} SecondEL (Tomáš Ryvola). Všechna práva vyhrazena.</p>
          <p className="text-center sm:text-right">
            Živnostenské oprávnění evidováno u Živnostenského úřadu Klatovy.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
