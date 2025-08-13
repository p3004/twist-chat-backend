package chat.twist.com.di

import chat.twist.com.repository.ChatRepository
import chat.twist.com.repository.MessageRepository
import chat.twist.com.repository.UserRepository
import org.koin.dsl.module

val appModule = module {
    single { UserRepository() }
    single { ChatRepository() }
    single { MessageRepository() }
}