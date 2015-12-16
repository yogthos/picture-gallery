-- name: create-user!
-- creates a new user record
INSERT INTO users
(id, pass)
VALUES (:id, :pass)

-- name: update-user!
-- update an existing user record
UPDATE users
SET email = :email
WHERE id = :id

-- name: get-user
-- retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id

--name: save-file!
-- saves a file to the database
INSERT INTO files
(owner, type, name, data)
VALUES (:owner, :type, :name, :data)

-- name: thumbnails-for-user
-- retrieves thumbnails ids for the user
SELECT name FROM files WHERE owner = :identity

--START:image-queries
--name: list-thumbnails
-- selects thumbnail names for the given gallery owner
SELECT owner, name FROM files
 WHERE owner = :owner
  AND name LIKE 'thumb\_%'

--name: get-image
-- retrieve image data by name
SELECT type, data FROM files
WHERE name = :name
--END:image-queries

--START:gallery-previews
--name: select-gallery-previews
-- selects a thumbnail for each user gallery
WITH summary AS (
    SELECT f.owner,
           f.name,
           ROW_NUMBER() OVER(PARTITION BY f.owner
                                 ORDER BY f.name DESC) AS rk
      FROM files f WHERE name like 'thumb\_%')
SELECT s.*
  FROM summary s
 WHERE s.rk = 1
--END:gallery-previews

--START:delete-file
--name: delete-file!
-- deletes the file with the given name and owner
DELETE FROM files
WHERE name = :name
AND owner = :owner
--END:delete-file

--START:delete-user
--name: delete-user!
-- deletes the user account
DELETE FROM users
WHERE id = :id

--name: delete-user-images!
-- deletes all the images for the specified user
DELETE FROM files
WHERE owner = :owner
--END:delete-user