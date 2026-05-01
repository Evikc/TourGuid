package com.stepcounter.app.data;

import com.stepcounter.app.data.local.DailyStepsDao;
import com.stepcounter.app.data.local.StepsPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class StepsRepository_Factory implements Factory<StepsRepository> {
  private final Provider<DailyStepsDao> daoProvider;

  private final Provider<StepsPreferences> prefsProvider;

  public StepsRepository_Factory(Provider<DailyStepsDao> daoProvider,
      Provider<StepsPreferences> prefsProvider) {
    this.daoProvider = daoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public StepsRepository get() {
    return newInstance(daoProvider.get(), prefsProvider.get());
  }

  public static StepsRepository_Factory create(Provider<DailyStepsDao> daoProvider,
      Provider<StepsPreferences> prefsProvider) {
    return new StepsRepository_Factory(daoProvider, prefsProvider);
  }

  public static StepsRepository newInstance(DailyStepsDao dao, StepsPreferences prefs) {
    return new StepsRepository(dao, prefs);
  }
}
