import {useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import CreateAdForm from '../components/CreateAdForm';
import {getUserRole} from '../services/authService';

const CreateAdPage = () => {
    const [userRole, setUserRole] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        getUserRole()
            .then((role) => {
                setUserRole(role);
                if (role !== 'ROLE_ADMIN') {
                    navigate('/unauthorized'); // Redirect non-admin users
                }
            })
            .catch(() => {
                console.error('Failed to fetch user role');
                navigate('/unauthorized'); // Redirect on error
            });
    }, [navigate]);

    if (userRole !== 'ROLE_ADMIN') {
        return null; // Render nothing while checking the role
    }

  return (
    <div>
      <CreateAdForm />
    </div>
  );
};

export default CreateAdPage;

