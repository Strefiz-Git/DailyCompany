package com.dailycompany.di

import com.dailycompany.data.backup.BackupRepository
import com.dailycompany.data.backup.BackupSettingsSource
import com.dailycompany.data.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    /** O mesmo SettingsRepository serve para ler/escrever o DataStore E para o backup. */
    @Binds
    abstract fun bindBackupSettingsSource(impl: SettingsRepository): BackupSettingsSource

    /**
     * Implementação real em data/repository/BackupRepositoryImpl.kt: lê e escreve os DAOs
     * (categorias, peças, outfits, tarefas, eventos, lembretes, histórico de uso) dentro
     * de transações Room. Criar esse ficheiro segue android-data-offline + android-backup-restore.
     */
    // @Binds
    // abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}