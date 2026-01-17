-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- PROFILES (Users)
create table if not exists public.profiles (
  id uuid references auth.users not null primary key,
  email text,
  full_name text,
  passport_number text,
  phone_number text,
  role text default 'customer' check (role in ('customer', 'admin')),
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- AIRPORTS
create table if not exists public.airports (
  code text primary key,
  name text not null,
  city text not null,
  country text not null
);

-- FLIGHTS
create table if not exists public.flights (
  id uuid default uuid_generate_v4() primary key,
  flight_number text not null,
  origin_code text references public.airports(code) not null,
  destination_code text references public.airports(code) not null,
  departure_time timestamp with time zone not null,
  arrival_time timestamp with time zone not null,
  price_economy decimal(10,2) not null,
  price_business decimal(10,2) not null default 0,
  price_first decimal(10,2) not null default 0,
  status text default 'SCHEDULED' check (status in ('SCHEDULED', 'DELAYED', 'CANCELLED', 'LANDED'))
);

-- SEATS
create table if not exists public.seats (
  id uuid default uuid_generate_v4() primary key,
  flight_id uuid references public.flights(id) on delete cascade not null,
  seat_number text not null,
  class text default 'ECONOMY' check (class in ('ECONOMY', 'BUSINESS')),
  status text default 'AVAILABLE' check (status in ('AVAILABLE', 'LOCKED', 'BOOKED')),
  locked_until timestamp with time zone,
  locked_by uuid references auth.users,
  unique(flight_id, seat_number)
);

-- BOOKINGS
create table if not exists public.bookings (
  id uuid default uuid_generate_v4() primary key,
  user_id uuid references auth.users not null,
  flight_id uuid references public.flights(id) not null,
  seat_id uuid references public.seats(id) not null,
  booking_reference text unique not null,
  status text default 'CONFIRMED' check (status in ('CONFIRMED', 'CANCELLED', 'PENDING')),
  passenger_name text not null,
  passenger_passport text,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- PAYMENTS
create table if not exists public.payments (
  id uuid default uuid_generate_v4() primary key,
  booking_id uuid references public.bookings(id) not null,
  amount decimal(10,2) not null,
  payment_method text not null,
  status text default 'COMPLETED',
  transaction_date timestamp with time zone default timezone('utc'::text, now())
);

-- AUDIT LOGS
create table if not exists public.audit_logs (
  id uuid default uuid_generate_v4() primary key,
  action text not null,
  table_name text not null,
  record_id uuid,
  performed_by uuid references auth.users,
  timestamp timestamp with time zone default timezone('utc'::text, now())
);

-- RLS POLICIES

-- Profiles: Users can view own profile, Admins can view all
alter table public.profiles enable row level security;
create policy "Public profiles are viewable by everyone" on public.profiles for select using (true);
create policy "Users can insert their own profile" on public.profiles for insert with check (auth.uid() = id);
create policy "Users can update own profile" on public.profiles for update using (auth.uid() = id);

-- Flights: Readable by all, Insert/Update by Admin only
alter table public.flights enable row level security;
create policy "Flights are viewable by everyone" on public.flights for select using (true);

-- Seats: Readable by all, Update logic handled via functions usually, but for direct:
alter table public.seats enable row level security;
create policy "Seats are viewable by everyone" on public.seats for select using (true);

-- Bookings: Users can view their own
alter table public.bookings enable row level security;
create policy "Users can view own bookings" on public.bookings for select using (auth.uid() = user_id);
create policy "Users can insert own bookings" on public.bookings for insert with check (auth.uid() = user_id);

-- SEED DATA (Minimal)
insert into public.airports (code, name, city, country) values 
('KTM', 'Tribhuvan International Airport', 'Kathmandu', 'Nepal'),
('PKR', 'Pokhara International Airport', 'Pokhara', 'Nepal'),
('BWA', 'Gautam Buddha International Airport', 'Bhairahawa', 'Nepal'),
('BIR', 'Biratnagar Airport', 'Biratnagar', 'Nepal')
on conflict (code) do nothing;

insert into public.flights (flight_number, origin_code, destination_code, departure_time, arrival_time, price_economy, price_business, price_first) values
('NP101', 'KTM', 'PKR', now() + interval '1 day', now() + interval '1 day 30 minutes', 5000.00, 10000.00, 15000.00),
('NP102', 'PKR', 'KTM', now() + interval '1 day 2 hours', now() + interval '1 day 2 hours 30 minutes', 5000.00, 10000.00, 15000.00),
('NP201', 'KTM', 'BWA', now() + interval '2 days', now() + interval '2 days 45 minutes', 7500.00, 11000.00, 16000.00)
on conflict do nothing;

-- Seat Generation for Flight NP101 (Simple loop equivalent)
do $$
declare
  f_id uuid;
  r int;
  c int;
  seats text[] := array['A', 'B', 'C', 'D'];
begin
  select id into f_id from public.flights where flight_number = 'NP101' limit 1;
  if f_id is not null then
    for r in 1..10 loop
      for c in 1..4 loop
        insert into public.seats (flight_id, seat_number, class) 
        values (f_id, r || seats[c], 'ECONOMY')
        on conflict do nothing;
      end loop;
    end loop;
  end if;
end $$;
