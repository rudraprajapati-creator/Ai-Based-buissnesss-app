-- ==============================================================================
-- BizFlow Production PostgreSQL Schema for Supabase
-- Multi-tenant architecture with Row Level Security (RLS)
-- Designed for Indian Hardware Shops, Wholesalers, and Retail Businesses
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES (Supabase Auth Link)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    phone TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. BUSINESSES (Tenant Root)
CREATE TABLE IF NOT EXISTS public.businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    business_type TEXT NOT NULL DEFAULT 'Hardware', -- Hardware, Electrical, Plumbing, Wholesale, Other
    phone TEXT,
    email TEXT,
    address TEXT,
    gstin TEXT,
    currency TEXT DEFAULT '₹',
    invoice_prefix TEXT DEFAULT 'INV-2026-',
    default_gst_percent NUMERIC(5,2) DEFAULT 18.00,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. CUSTOMERS
CREATE TABLE IF NOT EXISTS public.customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    gstin TEXT,
    total_orders INTEGER DEFAULT 0,
    total_purchased NUMERIC(12,2) DEFAULT 0.00,
    outstanding_amount NUMERIC(12,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. PRODUCTS
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    sku TEXT,
    category TEXT DEFAULT 'Hardware',
    unit TEXT DEFAULT 'pcs',
    selling_price NUMERIC(10,2) NOT NULL,
    purchase_price NUMERIC(10,2) DEFAULT 0.00,
    gst_percentage NUMERIC(5,2) DEFAULT 18.00,
    current_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock INTEGER NOT NULL DEFAULT 10,
    supplier TEXT,
    is_archived BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. ORDERS
CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    order_number TEXT NOT NULL,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    customer_name TEXT NOT NULL,
    customer_phone TEXT,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    discount NUMERIC(12,2) DEFAULT 0.00,
    gst_total NUMERIC(12,2) DEFAULT 0.00,
    grand_total NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    order_status TEXT NOT NULL DEFAULT 'CONFIRMED', -- DRAFT, CONFIRMED, PROCESSING, READY, DELIVERED, CANCELLED
    payment_status TEXT NOT NULL DEFAULT 'UNPAID',  -- UNPAID, PARTIALLY_PAID, PAID
    amount_paid NUMERIC(12,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. ORDER ITEMS
CREATE TABLE IF NOT EXISTS public.order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE SET NULL,
    product_name TEXT NOT NULL,
    unit TEXT DEFAULT 'pcs',
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL,
    discount NUMERIC(10,2) DEFAULT 0.00,
    gst_percentage NUMERIC(5,2) DEFAULT 18.00,
    gst_amount NUMERIC(10,2) DEFAULT 0.00,
    total NUMERIC(12,2) NOT NULL
);

-- 7. INVOICES
CREATE TABLE IF NOT EXISTS public.invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    invoice_number TEXT NOT NULL,
    order_id UUID REFERENCES public.orders(id) ON DELETE SET NULL,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    customer_name TEXT NOT NULL,
    customer_gstin TEXT,
    customer_phone TEXT,
    customer_address TEXT,
    issue_date TIMESTAMPTZ DEFAULT NOW(),
    due_date TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '7 days'),
    subtotal NUMERIC(12,2) NOT NULL,
    tax_total NUMERIC(12,2) NOT NULL,
    grand_total NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2) DEFAULT 0.00,
    balance_due NUMERIC(12,2) NOT NULL,
    status TEXT NOT NULL DEFAULT 'UNPAID', -- UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. PAYMENTS
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    customer_name TEXT NOT NULL,
    order_id UUID REFERENCES public.orders(id) ON DELETE SET NULL,
    invoice_id UUID REFERENCES public.invoices(id) ON DELETE SET NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    payment_method TEXT NOT NULL DEFAULT 'UPI', -- Cash, UPI, Bank Transfer, Card, Cheque
    reference_id TEXT,
    payment_date TIMESTAMPTZ DEFAULT NOW(),
    notes TEXT
);

-- 9. INVENTORY MOVEMENTS (Stock Audit Trail)
CREATE TABLE IF NOT EXISTS public.inventory_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    product_name TEXT NOT NULL,
    type TEXT NOT NULL, -- IN, OUT, ADJUSTMENT, ORDER_FULFILLMENT, ORDER_CANCELLATION
    quantity_changed INTEGER NOT NULL,
    stock_after INTEGER NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 10. AI CONVERSATIONS & MESSAGES
CREATE TABLE IF NOT EXISTS public.ai_conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.ai_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.ai_conversations(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Strict multi-tenant isolation: A business can NEVER access another's records
-- ==============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.inventory_movements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_messages ENABLE ROW LEVEL SECURITY;

-- Helper function to check business ownership
CREATE OR REPLACE FUNCTION public.current_user_has_business_access(bid UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.businesses
        WHERE id = bid AND owner_id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Profiles Policy
CREATE POLICY "Users can manage own profile"
    ON public.profiles FOR ALL
    USING (auth.uid() = id);

-- Businesses Policy
CREATE POLICY "Owners can manage own businesses"
    ON public.businesses FOR ALL
    USING (auth.uid() = owner_id);

-- Customers Policy
CREATE POLICY "Business isolated customers"
    ON public.customers FOR ALL
    USING (public.current_user_has_business_access(business_id));

-- Products Policy
CREATE POLICY "Business isolated products"
    ON public.products FOR ALL
    USING (public.current_user_has_business_access(business_id));

-- Orders Policy
CREATE POLICY "Business isolated orders"
    ON public.orders FOR ALL
    USING (public.current_user_has_business_access(business_id));

-- Order Items Policy
CREATE POLICY "Business isolated order items"
    ON public.order_items FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_items.order_id
            AND public.current_user_has_business_access(orders.business_id)
        )
    );

-- Invoices Policy
CREATE POLICY "Business isolated invoices"
    ON public.invoices FOR ALL
    USING (public.current_user_has_business_access(business_id));

-- Payments Policy
CREATE POLICY "Business isolated payments"
    ON public.payments FOR ALL
    USING (public.current_user_has_business_access(business_id));

-- Inventory Movements Policy
CREATE POLICY "Business isolated inventory movements"
    ON public.inventory_movements FOR ALL
    USING (public.current_user_has_business_access(business_id));

-- AI Conversations & Messages Policy
CREATE POLICY "Business isolated AI conversations"
    ON public.ai_conversations FOR ALL
    USING (public.current_user_has_business_access(business_id));

CREATE POLICY "Business isolated AI messages"
    ON public.ai_messages FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.ai_conversations
            WHERE ai_conversations.id = ai_messages.conversation_id
            AND public.current_user_has_business_access(ai_conversations.business_id)
        )
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_customers_business ON public.customers(business_id);
CREATE INDEX IF NOT EXISTS idx_products_business ON public.products(business_id);
CREATE INDEX IF NOT EXISTS idx_orders_business ON public.orders(business_id);
CREATE INDEX IF NOT EXISTS idx_invoices_business ON public.invoices(business_id);
CREATE INDEX IF NOT EXISTS idx_payments_business ON public.payments(business_id);
CREATE INDEX IF NOT EXISTS idx_inventory_movements_business ON public.inventory_movements(business_id);
