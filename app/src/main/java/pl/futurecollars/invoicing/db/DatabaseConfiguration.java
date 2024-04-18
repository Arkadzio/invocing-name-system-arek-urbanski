package pl.futurecollars.invoicing.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.futurecollars.invoicing.db.file.FileBasedDatabase;
import pl.futurecollars.invoicing.db.file.IdService;
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase;
import pl.futurecollars.invoicing.utils.FilesService;
import pl.futurecollars.invoicing.utils.JsonService;

@Slf4j
@Configuration
public class DatabaseConfiguration {

  @Bean
  public IdService idService(FilesService filesService,
                             @Value("${invoicing-system.database.directory}") String databaseDirectory,
                             @Value("${invoicing-system.database.id.file}") String idFile
  ) throws IOException {
    Path idFilePath = Files.createTempFile(databaseDirectory, idFile);
    return new IdService(idFilePath, filesService);
  }

  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "file")
  @Bean
  public Database fileBasedDatabase(IdService idService,
                                    FilesService filesService,
                                    JsonService jsonService,
                                    @Value("${invoicing-system.database.directory}") String databaseDirectory,
                                    @Value("${invoicing-system.database.invoices.file}") String invoicesFile) throws IOException {
    Path databaseFilePath = Files.createTempFile(databaseDirectory, invoicesFile);
    log.trace("Używam file database - trace"); log.debug("Używam file database - debug"); log.info("Używam file database - info"); log.warn("Używam file database - warning"); log.error("Używam file database - error");
    return new FileBasedDatabase(databaseFilePath, idService, filesService, jsonService);
  }

  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "memory")
  @Bean
  public Database inMemoryDatabase() {
    log.info("Używam in memory database");
    return new InMemoryDatabase();
  }
}
