import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-gray-800 text-white mt-auto">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-lg font-semibold mb-4">O nás</h3>
            <p className="text-gray-400">
              Váš spolehlivý e-shop s širokým výběrem produktů.
            </p>
            <div className="mt-3">
              <Link to="/about" className="text-sm text-blue-400 hover:text-blue-300 transition-colors">
                Více informací &rarr;
              </Link>
            </div>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4">Kontakt</h3>
            <p className="text-gray-400">Email: info@eshop.cz</p>
            <p className="text-gray-400">Telefon: +420 123 456 789</p>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4">Odkazy</h3>
            <ul className="space-y-2 text-gray-400">
              <li>
                <Link to="/terms" className="hover:text-white transition-colors">
                  Obchodní podmínky
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="hover:text-white transition-colors">
                  Ochrana osobních údajů
                </Link>
              </li>
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; {new Date().getFullYear()} E-Shop. Všechna práva vyhrazena.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
