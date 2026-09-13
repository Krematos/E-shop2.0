import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

const Header = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <header className="bg-white shadow-md sticky top-0 z-50">
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link to="/" className="text-5xl font-bold text-primary-600">
            SecondEL
          </Link>

          <nav className="hidden text-2xl md:flex items-center space-x-6">
            <Link to="/products" className="text-gray-700 hover:text-primary-600 transition-colors">
              Produkty
            </Link>
            <Link to="/about" className="text-gray-700 hover:text-primary-600 transition-colors">
                O nás
            </Link>
            {isAuthenticated() && (
              <>
                {user && user.roles && user.roles.some(
                  (r) => r.authority === 'ROLE_ADMIN' || r === 'ROLE_ADMIN'
                ) && (
                  <Link to="/admin" className="text-gray-700 hover:text-primary-600 transition-colors">
                    Administrace
                  </Link>
                )}
              </>
            )}
          </nav>

          <div className="flex items-center space-x-4">
            {isAuthenticated() ? (
              <>
                <Link to="/profile" className="text-gray-700 hover:text-primary-600 font-medium hidden md:inline">
                  {user?.username || 'Profil'}
                </Link>
                <button
                  onClick={handleLogout}
                  className="btn-secondary text-sm"
                >
                  Odhlásit se
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-secondary text-sm">
                  Přihlásit se
                </Link>
                <Link to="/register" className="btn-primary text-sm">
                  Registrovat se
                </Link>
              </>
            )}
          </div>
        </div>

        {/* Mobile menu */}
        <nav className="md:hidden mt-4 flex flex-wrap gap-2">
          <Link to="/" className="text-sm text-gray-700 hover:text-primary-600">
            Domů
          </Link>
          <Link to="/products" className="text-sm text-gray-700 hover:text-primary-600">
            Produkty
          </Link>
          {isAuthenticated() && (
            <>

              <Link to="/profile" className="text-sm text-gray-700 hover:text-primary-600">
                {user?.username || 'Profil'}
              </Link>
              {user.roles && user.roles.some(
                (r) => r.authority === 'ROLE_ADMIN' || r === 'ROLE_ADMIN'
              ) && (
                <Link to="/admin" className="text-sm text-gray-700 hover:text-primary-600">
                  Administrace
                </Link>
              )}
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;
