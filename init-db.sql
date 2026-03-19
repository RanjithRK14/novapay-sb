-- userdb already created as POSTGRES_DB above
-- create the remaining 4 service databases

CREATE DATABASE walletdb;
CREATE DATABASE transactiondb;
CREATE DATABASE notificationdb;
CREATE DATABASE rewarddb;

-- Grant all privileges to the paypal user
GRANT ALL PRIVILEGES ON DATABASE userdb       TO paypal;
GRANT ALL PRIVILEGES ON DATABASE walletdb     TO paypal;
GRANT ALL PRIVILEGES ON DATABASE transactiondb TO paypal;
GRANT ALL PRIVILEGES ON DATABASE notificationdb TO paypal;
GRANT ALL PRIVILEGES ON DATABASE rewarddb     TO paypal;
