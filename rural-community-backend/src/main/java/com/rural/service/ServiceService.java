package com.rural.service;

import com.rural.common.PageResult;
import com.rural.dto.ServiceApplyRequest;
import com.rural.dto.ServiceHandleRequest;
import com.rural.dto.ServiceRejectRequest;
import com.rural.entity.ServiceApply;
import com.rural.entity.ServiceItem;

import java.util.List;

public interface ServiceService {

    List<ServiceItem> listItems();

    ServiceItem itemDetail(Long id);

    PageResult<ServiceApply> listApplies(int pageNum, int pageSize);

    ServiceApply applyDetail(Long id);

    ServiceApply apply(ServiceApplyRequest request);

    void accept(Long id);

    /** 网格员初审通过 */
    void gridPass(Long id, ServiceHandleRequest request);

    /** 网格员初审不通过 */
    void gridReject(Long id, ServiceRejectRequest request);

    void finish(Long id, ServiceHandleRequest request);

    /** 村委审核通过（无需网格初审或初筛通过） */
    void approve(Long id);

    /** 村委驳回申请并说明理由 */
    void reject(Long id, ServiceRejectRequest request);
}
