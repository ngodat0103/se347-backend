package com.github.ngodat0103.authsvc.dto.mapper;

import com.github.ngodat0103.authsvc.dto.kafka.SiteUserTopicDto;
import com.github.ngodat0103.authsvc.persistence.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-13T22:22:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Ubuntu)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User mapToEntity(SiteUserTopicDto siteUserTopicDto) {
        if ( siteUserTopicDto == null ) {
            return null;
        }

        User user = new User();

        user.setId( siteUserTopicDto.id() );
        user.setEmailAddress( siteUserTopicDto.emailAddress() );
        user.setHashedPassword( siteUserTopicDto.hashedPassword() );

        return user;
    }
}
