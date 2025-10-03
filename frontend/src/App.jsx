import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { fetchProducts } from './api/productService';
import Container from './components/Container';
import Menu from './components/Menu';
import Header from './components/Header';
import ProductList from './components/ProductList';
import Login from './components/Login';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProductPage from './pages/ProductPage';


function App() {
  return (
    <Container>
          <Header />
          <Menu />
          <main>
            <Routes>
              <Route path="/" element={<ProductPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Routes>
          </main>
        </Container>
  );
}

export default App;
