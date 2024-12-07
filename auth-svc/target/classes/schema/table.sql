-- TABLE: ROLE_GROUPS
CREATE TABLE ROLE_GROUPS (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL
);

-- TABLE: ROLES
CREATE TABLE ROLES (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL
);

-- TABLE: ROLE_GROUPS_ROLES
CREATE TABLE ROLE_GROUPS_ROLES (
                                   id SERIAL PRIMARY KEY,
                                   role_groups_id INT REFERENCES ROLE_GROUPS(id) ON DELETE CASCADE,
                                   role_id INT REFERENCES ROLES(id) ON DELETE CASCADE
);

-- TABLE: PERMISSIONS
CREATE TABLE PERMISSIONS (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL
);

-- TABLE: ROLES_PERMISSIONS
CREATE TABLE ROLES_PERMISSIONS (
                                   roles_id INT REFERENCES ROLES(id) ON DELETE CASCADE,
                                   permissions_id INT REFERENCES PERMISSIONS(id) ON DELETE CASCADE,
                                   PRIMARY KEY (roles_id,permissions_id)
);

-- TABLE: MODULES
CREATE TABLE MODULES (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL
);

-- TABLE: MODULES_PERMISSIONS
CREATE TABLE MODULES_PERMISSIONS (
                                     id SERIAL PRIMARY KEY,
                                     modules_id INT REFERENCES MODULES(id) ON DELETE CASCADE,
                                     permissions_id INT REFERENCES PERMISSIONS(id) ON DELETE CASCADE
);

-- TABLE: OPERATIONS
CREATE TABLE OPERATIONS (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

-- TABLE: PERMISSIONS_OPERATIONS
CREATE TABLE PERMISSIONS_OPERATIONS (
                                        id SERIAL PRIMARY KEY,
                                        permission_id INT REFERENCES PERMISSIONS(id) ON DELETE CASCADE,
                                        operations_id INT REFERENCES OPERATIONS(id) ON DELETE CASCADE
);
