package com.shop.service.module.service;

import com.shop.service.module.entity.Result;

public interface TeamService extends BaseService {
    Result getTeamListForPage(int pno, int psize, String name, Integer isOnSale, Integer type);

    Result setOnSale(Long id, Integer isOnSale);

    Result insertTeam(Long id, Long id1);

    Result getResult(Long id, Long id1);

    Result getMyTeamListForPage(int pno, int psize, Long id, Integer type);

    void makeActivityTimeout(Long id);

    Result getTeamUserList(Long id);
}
