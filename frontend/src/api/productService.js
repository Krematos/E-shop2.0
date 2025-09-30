export async function fetchProducts () {
  const response = await fetch('http://localhost:8080/api/products'); // URL tvého Spring Boot endpointu
  if (!response.ok) {
    throw new Error('Nepodařilo se načíst produkty');
  }
  return response.json(); // vrací pole produktů
}

export async function addProduct(product, token) {
  const response = await fetch('http://localhost:8080/api/ProductController', {
    method: "POST",
    headers: {
        'Content-Type': 'application/json',
        "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify(product)
  });
  if (!response.ok) {
    throw new Error('Nepodařilo se přidat produkt');
  }
  return response.json(); // vrací přidaný produkt
}

export async function deleteProduct(productId, token) {
   const response = await fetch(`http://localhost:8080/api/products/${productId}`, {
    method: "DELETE",
    headers: {
        "Authorization": `Bearer ${token}`
    }
    });
}



