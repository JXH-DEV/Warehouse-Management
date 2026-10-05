# Warehouse Management

A desktop app for running a small warehouse: keeping track of products and stock levels, handling incoming and outgoing orders, and following shipments until they arrive. It's written in Java with Swing, and all the data lives in a PostgreSQL database hosted on Supabase.

The interface is in Albanian ("Menaxhimi i Magazinës"), and so are the table and column names in the database.

## What it does

- **Dashboard.** A quick overview when you log in: products that have dropped to or below their minimum stock, and the most recent orders.
- **Products.** Add, edit, delete and search products, plus a quick stock adjustment button. Each one has a category, a unit (pieces, kg, etc.), a price, the quantity on hand and a minimum stock level.
- **Orders.** Create incoming orders (purchases from a supplier) and outgoing orders (sales to a client), each with one or more line items. You can filter the list by type. Stock is updated when an order is marked as delivered (incoming orders add to stock, outgoing ones take away), and the order remembers that it has been applied, so quantities don't get counted twice. If you move a delivered order back to another status, the change is reversed. An outgoing order is refused if there isn't enough stock.
- **Shipments.** Track shipments against orders: tracking number, carrier, destination, weight, and the shipped and estimated arrival dates.
- **Users.** Admins can add, edit and remove accounts. The app won't let you demote or delete the last remaining admin.

### Roles

There are three roles, picked when the account is created:

| Role | Can do |
| --- | --- |
| `ADMIN` | Everything, including managing users |
| `MANAGER` | Add, edit and delete products, orders and shipments |
| `OPERATOR` | View products, orders and shipments, but not change them |

## Getting it running

You'll need Java 11 or newer and a Supabase project (the free tier is fine).

### 1. Set up the database

Open the SQL Editor in your Supabase dashboard and run `database/supabase_schema.sql`. It creates the tables and adds some sample products, orders and shipments so there's something to look at.

Heads up: the script starts by dropping the existing tables, so only run it on a database you're happy to reset.

### 2. Add your connection details

Copy the example config and fill it in:

```
cp config/database.properties.example config/database.properties
```

Then edit `config/database.properties`:

```
db.url=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
db.user=postgres.<project-ref>
db.password=<your-database-password>
```

You'll find the host and port in Supabase under **Connect → Session pooler**. The pooler is the better choice on most home networks, because the direct `db.<project-ref>.supabase.co` address needs IPv6 and often doesn't work on Windows. The direct option is still in the example file if your network supports it.

The app looks for this file in a `config` folder relative to where you launch it, so start it from the project root.

### 3. Run it

The easiest way is to open the folder in NetBeans (it's a standard Ant project) and hit Run. The main class is `warehouse.Main`.

From the command line, with the project root as your working directory:

```
javac -cp lib/postgresql-42.7.4.jar -d build/classes $(find src -name "*.java")
java -cp "build/classes:lib/postgresql-42.7.4.jar" warehouse.Main
```

On Windows, use `;` instead of `:` in the classpath, and compile with NetBeans or list the source files by hand instead of using `find`.

If the connection fails, you'll get an error dialog on startup and the app will close. Check the URL, user and password first.

### Sample logins

The schema script creates three accounts:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123` | Admin |
| `manager` | `man123` | Manager |
| `operator` | `op123` | Operator |

Change or delete these before you put any real data in.

## Project layout

```
src/warehouse/
  Main.java          starts the app and checks the database connection
  dao/               database access (DataStore, connection and config)
  model/             Product, Order, OrderItem, Shipment, User
  view/              the Swing screens (login, main frame, one panel per section)
  util/              UITheme, the shared look and feel
config/              database connection settings
database/            supabase_schema.sql
lib/                 PostgreSQL JDBC driver
```

## Things to know before using this for real

- **Passwords are stored and compared as plain text.** That's fine for a class project or a demo, but it should be replaced with hashing (bcrypt or similar) before anyone relies on it.
- **Keep `config/database.properties` out of version control.** It holds your database password. Add it to `.gitignore` and commit only the `.example` file.
- `data/warehouse.dat` is left over from an earlier version that stored everything in a local file. The app doesn't read it anymore.

## Built with

- Java 11, Swing
- PostgreSQL on Supabase
- PostgreSQL JDBC driver 42.7.4
- Ant / NetBeans for the build
