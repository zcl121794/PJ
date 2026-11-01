package com.rural.service;

import com.rural.common.PageResult;
import com.rural.dto.AppealAssignRequest;
import com.rural.dto.AppealDissatisfiedRequest;
import com.rural.dto.AppealFollowUpRequest;
import com.rural.dto.AppealHandleRequest;
import com.rural.dto.AppealReportRequest;
import com.rural.dto.AppealRequest;
import com.rural.dto.AppealReturnRequest;
import com.rural.dto.AppealUrgeRequest;
import com.rural.entity.Appeal;
import com.rural.entity.AppealFlow;

import java.util.List;

public interface AppealService {

    PageResult<Appeal> list(int pageNum, int pageSize, String category, String keyword);

    Appeal detail(Long id);

    List<AppealFlow> flows(Long id);

    Appeal submit(AppealRequest request);

    void accept(Long id);

    void assign(Long id, AppealAssignRequest request);

    void handle(Long id, AppealHandleRequest request);

    /** 网格员上报村委处理 */
    void reportToCommittee(Long id, AppealReportRequest request);

    /** 审核通过（已答复 → 已办结） */
    void finish(Long id);

    /** 审核退回重办（已答复 → 处理中），并说明理由 */
    void rejectHandling(Long id, AppealReturnRequest request);

    /** 村委修改处理结果 */
    void updateProcessResult(Long id, AppealHandleRequest request);

    /** 村委催办处理中的诉求 */
    void urge(Long id, AppealUrgeRequest request);

    /** 村民对处理反馈表示满意 */
    void satisfied(Long id);

    /** 村民对处理反馈不满意，并说明理由（转回处理中） */
    void dissatisfied(Long id, AppealDissatisfiedRequest request);

    /** 村民追问问题（转回处理中继续办理） */
    void followUp(Long id, AppealFollowUpRequest request);
}
