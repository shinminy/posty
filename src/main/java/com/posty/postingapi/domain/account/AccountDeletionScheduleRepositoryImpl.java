package com.posty.postingapi.domain.account;

import com.posty.postingapi.domain.common.ScheduleStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AccountDeletionScheduleRepositoryImpl implements AccountDeletionScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AccountDeletionScheduleRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<AccountDeletionSchedule> findScheduledBefore(LocalDateTime cutoff) {
        QAccountDeletionSchedule schedule = QAccountDeletionSchedule.accountDeletionSchedule;

        return queryFactory
                .selectFrom(schedule)
                .where(
                        schedule.status.eq(ScheduleStatus.SCHEDULED),
                        schedule.scheduledAt.before(cutoff)
                )
                .fetch();
    }
}
