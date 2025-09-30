import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { fetchProducts } from './api/productService';
import Container from './components/Container';
import Menu from './components/Menu';
import Header from './components/Header';
import ProductList from './components/ProductList';
import Login from './components/Login';

function App() {
  return (
    <Container>
          <Header />
          <Menu />
          <main>
            <h2>Vítejte v e-shopu</h2>
            <p>Zde budou produkty…</p>
          </main>
        </Container>
  );
}

export default App;
