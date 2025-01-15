# AuthService
Authentication Service for Catan App

## Uses
- Register a user
- Login a user and return a JWT token
- Manage and Update User(s) account(s) depending on ROLE.

## Stories
### User Stories
- Login
- Register
- Validate (JWT)
- Change Password
- Delete Account

### Admin Stories
- Same as User
- View All Users
- Manage Users
- Delete User accounts
- Ban User Accounts
- Lock/Unlock Accounts

## Entites
### User
- UUID id - primary key
- first_name VARCHAR
- email VARCHAR unique (hashed)
- password VARCHAR (hashed)
- username VARCHAR unique
- role ENUM (player, admin)


## Future Integrations
- Role integration with game lobbies to allow a player who created the lobby to be a lobby owner and have basic control permissions over the lobby
- Email verification for user registration and account activation
- Email verification for password resets
- Email notifications of account changes
