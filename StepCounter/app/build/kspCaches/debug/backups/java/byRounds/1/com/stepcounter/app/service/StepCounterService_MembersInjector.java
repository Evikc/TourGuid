package com.stepcounter.app.service;

import com.stepcounter.app.data.StepsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class StepCounterService_MembersInjector implements MembersInjector<StepCounterService> {
  private final Provider<StepsRepository> repositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public StepCounterService_MembersInjector(Provider<StepsRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<StepCounterService> create(
      Provider<StepsRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new StepCounterService_MembersInjector(repositoryProvider, notificationHelperProvider);
  }

  @Override
  public void injectMembers(StepCounterService instance) {
    injectRepository(instance, repositoryProvider.get());
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("com.stepcounter.app.service.StepCounterService.repository")
  public static void injectRepository(StepCounterService instance, StepsRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.stepcounter.app.service.StepCounterService.notificationHelper")
  public static void injectNotificationHelper(StepCounterService instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
