package com.team1.hrbank.mapper;

import com.team1.hrbank.dto.BackupDto;
import com.team1.hrbank.entity.Backup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")  // Spring Bean으로 등록
public interface BackupMapper {

  // 이름이 같은 필드는 자동 매핑
  // 다른 필드는 @Mapping으로 직접 지정
  @Mapping(target = "startedAt", expression = "java(backup.getStartedAt() != null ? backup.getStartedAt().toString() : null)")
  @Mapping(target = "endedAt",   expression = "java(backup.getEndedAt() != null ? backup.getEndedAt().toString() : null)")
  @Mapping(target = "status",    expression = "java(backup.getStatus().name())")
  @Mapping(target = "fileId",
      expression = "java(backup.getBackupFile() != null ? backup.getBackupFile().getId() : null)")
  BackupDto toDto(Backup backup);
}
