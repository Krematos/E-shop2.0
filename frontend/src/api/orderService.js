const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/orders";

// Funkce, která získá všechny produkty z backendu
export async function fetchProducts() {
  const response = await fetch('http://localhost:8080/api/products'); // URL tvého Spring Boot endpointu
  if (!response.ok) {
    throw new Error('Nepodařilo se načíst produkty');
  }
  return response.json(); // vrací pole produktů
}