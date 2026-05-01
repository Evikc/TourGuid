package com.stepcounter.app.di;

import com.stepcounter.app.data.local.DailyStepsDao;
import com.stepcounter.app.data.local.StepsDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DatabaseModule_ProvideDailyStepsDaoFactory implements Factory<DailyStepsDao> {
  private final Provider<StepsDatabase> dbProvider;

  public DatabaseModule_ProvideDailyStepsDaoFactory(Provider<StepsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DailyStepsDao get() {
    return provideDailyStepsDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDailyStepsDaoFactory create(
      Provider<StepsDatabase> dbProvider) {
    return new DatabaseModule_ProvideDailyStepsDaoFactory(dbProvider);
  }

  public static DailyStepsDao provideDailyStepsDao(StepsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDailyStepsDao(db));
  }
}
