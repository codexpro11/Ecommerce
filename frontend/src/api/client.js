const API_BASE = import.meta.env.VITE_API_BASE || '/api';

export async function getProducts() {
  const res = await fetch(`${API_BASE}/products`);
  if (!res.ok) throw new Error('Failed to fetch products');
  return res.json();
}

export async function getProductById(id) {
  const res = await fetch(`${API_BASE}/product/${id}`);
  if (!res.ok) throw new Error('Product not found');
  return res.json();
}

export function getProductImageUrl(id) {
  return `${API_BASE}/product/${id}/image`;
}

export async function searchProducts(keyword) {
  const res = await fetch(`${API_BASE}/product/search?keyword=${encodeURIComponent(keyword)}`);
  if (!res.ok) throw new Error('Search failed');
  return res.json();
}

export async function addProduct(product, imageFile) {
  const formData = new FormData();
  formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }));
  if (imageFile) formData.append('imageFile', imageFile);
  const res = await fetch(`${API_BASE}/product`, {
    method: 'POST',
    body: formData,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Failed to add product');
  }
  return res.json();
}

export async function updateProduct(id, product, imageFile) {
  const formData = new FormData();
  formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }));
  if (imageFile) formData.append('imageFile', imageFile);
  const res = await fetch(`${API_BASE}/product/${id}`, {
    method: 'PUT',
    body: formData,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Failed to update product');
  }
  return res.json();
}

export async function getWishlist() {
  const res = await fetch(`${API_BASE}/wishlist/`);
  if (!res.ok) throw new Error('Failed to fetch wishlist');
  return res.json();
}

export async function toggleWishlist(item) {
  const res = await fetch(`${API_BASE}/wishlist/toggle`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(item),
  });
  if (!res.ok) throw new Error('Wishlist update failed');
  return res.json();
}

export async function placeOrder(orderRequest) {
  const res = await fetch(`${API_BASE}/orders/placed`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(orderRequest),
  });
  if (!res.ok) throw new Error('Order failed');
  return res.json();
}

export async function getAllOrders() {
  const res = await fetch(`${API_BASE}/orders`);
  if (!res.ok) throw new Error('Failed to fetch orders');
  return res.json();
}

// ——— Chatbot ———
export async function askChat(message) {
  const res = await fetch(`${API_BASE}/chat/ask?message=${encodeURIComponent(message)}`);
  if (!res.ok) throw new Error('Chat request failed');
  return res.text();
}

// ——— AI description generation ———
export async function generateDescription(productName, category) {
  const params = new URLSearchParams({ productName: productName || '', category: category || '' });
  const res = await fetch(`${API_BASE}/product/generate-description?${params}`, { method: 'POST' });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Failed to generate description');
  }
  return res.text();
}

// ——— AI image generation (returns image blob) ———
export async function generateImage(productName, description, category) {
  const params = new URLSearchParams({
    productName: productName || '',
    description: description || '',
    category: category || '',
  });
  const res = await fetch(`${API_BASE}/product/generate-image?${params}`, { method: 'POST' });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Failed to generate image');
  }
  return res.blob();
}
