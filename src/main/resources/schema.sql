-- Enable pgcrypto extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- USERS TABLE
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       firebase_uid VARCHAR(128) UNIQUE NOT NULL,
                       username VARCHAR(64) UNIQUE NOT NULL,
                       display_name VARCHAR(128),
                       avatar_url TEXT,
                       created_at TIMESTAMP DEFAULT NOW(),
                       email VARCHAR(255) DEFAULT 'default@email.com',
                       bio VARCHAR(255)
);

-- CHATS TABLE
CREATE TABLE chats (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       is_group BOOLEAN NOT NULL,
                       name VARCHAR(128),
                       created_by UUID REFERENCES users(id),
                       created_at TIMESTAMP DEFAULT NOW(),
                       mode VARCHAR(32) DEFAULT 'normal' -- 'normal', 'emoji_only', 'secret', 'draw'
);

-- CHAT MEMBERS TABLE
CREATE TABLE chat_members (
                              chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
                              user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                              joined_at TIMESTAMP DEFAULT NOW(),
                              PRIMARY KEY (chat_id, user_id)
);

-- MESSAGES TABLE
CREATE TABLE messages (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
                          sender_id UUID REFERENCES users(id),
                          content TEXT,
                          message_type VARCHAR(32) DEFAULT 'text', -- 'text', 'emoji', 'image', 'drawing', 'voice'
                          created_at TIMESTAMP DEFAULT NOW(),
                          is_deleted BOOLEAN DEFAULT FALSE,
                          expires_at TIMESTAMP, -- for secret chat auto-delete
                          extra JSONB -- for storing drawing data, etc.
);

-- MEDIA TABLE
CREATE TABLE media (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
                       url TEXT NOT NULL,
                       media_type VARCHAR(32), -- 'image', 'voice', 'drawing'
                       uploaded_at TIMESTAMP DEFAULT NOW()
);

-- (OPTIONAL) CHAT MODES TABLE
CREATE TABLE chat_modes (
                            chat_id UUID PRIMARY KEY REFERENCES chats(id) ON DELETE CASCADE,
                            mode VARCHAR(32) NOT NULL, -- 'normal', 'emoji_only', 'secret', 'draw'
                            emoji_only_until TIMESTAMP,
                            secret_chat_expires TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_messages_chat_id ON messages(chat_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_media_message_id ON media(message_id);
CREATE INDEX idx_chat_members_user_id ON chat_members(user_id);

-- Add some helpful comments
COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON TABLE chats IS 'Stores chat conversations, both one-on-one and group chats';
COMMENT ON TABLE chat_members IS 'Junction table linking users to chats they participate in';
COMMENT ON TABLE messages IS 'Stores all messages sent in chats with various types and features';
COMMENT ON TABLE media IS 'Stores media files attached to messages';
COMMENT ON TABLE chat_modes IS 'Optional table for storing additional chat mode settings';