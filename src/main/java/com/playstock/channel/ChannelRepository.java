package com.playstock.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByYoutubeChannelId(String youtubeChannelId);

    boolean existsByYoutubeChannelId(String youtubeChannelId);

    List<Channel> findByCreatorId(Long creatorId);
}
