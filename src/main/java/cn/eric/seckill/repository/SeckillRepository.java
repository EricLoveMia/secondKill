package cn.eric.seckill.repository;

import cn.eric.seckill.common.entity.Seckill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeckillRepository extends JpaRepository<Seckill, Long> {
	
	
}
