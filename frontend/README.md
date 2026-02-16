# ProductSite Frontend

React + Vite frontend for the ProductSite e-commerce backend.

## Setup

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```

2. Ensure the Spring Boot backend is running on **http://localhost:8080**.

3. Start the dev server:
   ```bash
   npm run dev
   ```

4. Open **http://localhost:5173** in your browser.

## Features

- **Home** – Hero and featured products
- **Products** – Grid with search and wishlist toggle
- **Product detail** – Single product, add to cart, wishlist
- **Cart** – Update quantity, remove items, proceed to checkout
- **Checkout** – Name, email, place order (calls backend `/api/orders/placed`)
- **Wishlist** – Toggle items via `/api/wishlist/toggle`

All API calls target `http://localhost:8080/api`. CORS is configured on the backend for `http://localhost:5173`.
