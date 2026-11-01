package com.rural.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rural.common.PageResult;
import com.rural.common.ResultCode;
import com.rural.common.exception.BusinessException;
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
import com.rural.entity.SysUser;
import com.rural.mapper.AppealFlowMapper;
import com.rural.mapper.AppealMapper;
import com.rural.mapper.SysUserMapper;
import com.rural.security.LoginUser;
import com.rural.security.SecurityUtils;
import com.rural.service.AppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AppealMapper appealMapper;
    private final AppealFlowMapper appealFlowMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<Appeal> list(int pageNum, int pageSize, String category, String keyword) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("deleted = 0")
                .eq(StringUtils.hasText(category), Appeal::getCategory, category)
                .like(StringUtils.hasText(keyword), Appeal::getTitle, keyword)
                .orderByDesc(Appeal::getSubmitTime);
        Page<Appeal> page = new Page<>(pageNum, pageSize);
        IPage<Appeal> result = appealMapper.selectPageByScope(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public Appeal detail(Long id) {
        Appeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "诉求不存在");
        }
        return appeal;
    }

    @Override
    public List<AppealFlow> flows(Long id) {
        return appealFlowMapper.selectList(
                new LambdaQueryWrapper<AppealFlow>()
                        .eq(AppealFlow::getAppealId, id)
                        .orderByAsc(AppealFlow::getOperateTime));
    }

    @Override
    @Transactional
    public Appeal submit(AppealRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "系统管理员不能提交诉求");
        }
        Appeal appeal = new Appeal();
        appeal.setAppealNo(generateNo("AP"));
        appeal.setCategory(request.getCategory());
        appeal.setTitle(request.getTitle());
        appeal.setContent(request.getContent());
        appeal.setLocation(request.getLocation());
        appeal.setIsAnonymous(request.getIsAnonymous() != null && request.getIsAnonymous() == 1 ? 1 : 0);
        appeal.setVillagerId(appeal.getIsAnonymous() == 1 ? null : loginUser.getVillagerId());
        appeal.setStatus(0);
        appeal.setSubmitTime(LocalDateTime.now());
        appealMapper.insert(appeal);
        saveFlow(appeal.getId(), 1, loginUser.getUserId(), "提交诉求");
        return appeal;
    }

    @Override
    @Transactional
    public void accept(Long id) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Appeal appeal = getById(id);
        if (appeal.getStatus() != 0) {
            throw new BusinessException("当前状态不可受理");
        }
        appeal.setStatus(1);
        appeal.setHandlerId(loginUser.getUserId());
        appeal.setAcceptTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
        saveFlow(id, 2, loginUser.getUserId(), "受理诉求");
    }

    @Override
    @Transactional
    public void assign(Long id, AppealAssignRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Appeal appeal = getById(id);
        if (appeal.getStatus() != 1) {
            throw new BusinessException("当前状态不可分派");
        }
        SysUser processor = sysUserMapper.selectById(request.getProcessorId());
        if (processor == null || processor.getStatus() == null || processor.getStatus() != 1) {
            throw new BusinessException("处理人不存在或已停用");
        }
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(request.getProcessorId());
        boolean assignable = roleCodes != null && roleCodes.stream()
                .anyMatch(role -> "GRID_WORKER".equals(role) || "COMMITTEE".equals(role));
        if (!assignable) {
            throw new BusinessException("只能分派给网格员或村委工作人员");
        }
        appeal.setStatus(2);
        appeal.setProcessorId(request.getProcessorId());
        appealMapper.updateById(appeal);
        saveFlow(id, 3, loginUser.getUserId(), "分派处理人");
    }

    @Override
    @Transactional
    public void handle(Long id, AppealHandleRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Appeal appeal = getById(id);
        if (loginUser.getRoles().contains("GRID_WORKER")) {
            // 网格员：待处理(0待受理/1已受理)可直接处理，处理中(2)填写处理结果
            int status = appeal.getStatus() == null ? -1 : appeal.getStatus();
            if (status < 0 || status > 2) {
                throw new BusinessException("当前状态不可处理");
            }
            if (appeal.getProcessorId() != null
                    && !Objects.equals(appeal.getProcessorId(), loginUser.getUserId())) {
                throw new BusinessException("该诉求未分派给您，无法处理");
            }
            if (appeal.getProcessorId() == null) {
                appeal.setProcessorId(loginUser.getUserId());
            }
            // 网格员处理后进入“已答复”，由村委审核
            appeal.setStatus(3);
            appeal.setProcessResult(request.getProcessResult());
            appealMapper.updateById(appeal);
            saveFlow(id, 6, loginUser.getUserId(), request.getProcessResult());
            return;
        }

        // 村委：仅已受理(1)可直接处理；分派后进入处理中(2)只能催办
        if (appeal.getStatus() == null || appeal.getStatus() != 1) {
            throw new BusinessException("当前状态不可处理");
        }
        // 村委直接处理即办结，无需再审核
        if (appeal.getProcessorId() == null) {
            appeal.setProcessorId(loginUser.getUserId());
        }
        appeal.setStatus(4);
        appeal.setFinishTime(LocalDateTime.now());
        appeal.setProcessResult(request.getProcessResult());
        appealMapper.updateById(appeal);
        saveFlow(id, 6, loginUser.getUserId(), request.getProcessResult());
        saveFlow(id, 7, loginUser.getUserId(), "直接处理办结");
    }

    @Override
    @Transactional
    public void reportToCommittee(Long id, AppealReportRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Appeal appeal = getGridWorkerAppeal(id, loginUser, "上报村委", 0, 1, 2);
        String comment = request != null && StringUtils.hasText(request.getComment())
                ? request.getComment().trim() : "网格员无法直接处理，上报村委协调";
        // 上报后统一回到村委待办理的“已受理”状态
        transferBack(appeal, 10, loginUser.getUserId(), "上报村委：" + comment, 1);
    }

    @Override
    @Transactional
    public void finish(Long id) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkCommittee(loginUser, "审核通过");
        Appeal appeal = getById(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 3) {
            throw new BusinessException("当前状态不可审核通过");
        }
        if (!StringUtils.hasText(appeal.getProcessResult())) {
            throw new BusinessException("处理结果为空，无法审核通过");
        }
        appeal.setStatus(4);
        appeal.setFinishTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
        saveFlow(id, 7, loginUser.getUserId(), "审核通过");
    }

    @Override
    @Transactional
    public void rejectHandling(Long id, AppealReturnRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkCommittee(loginUser, "审核退回");
        Appeal appeal = getById(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 3) {
            throw new BusinessException("仅已答复诉求可退回重办");
        }
        appealMapper.update(null, new LambdaUpdateWrapper<Appeal>()
                .eq(Appeal::getId, id)
                .set(Appeal::getStatus, 2)
                .set(Appeal::getFinishTime, null)
                .set(Appeal::getUpdateBy, loginUser.getUserId())
                .set(Appeal::getUpdateTime, LocalDateTime.now()));
        saveFlow(id, 12, loginUser.getUserId(), "审核退回：" + request.getReason().trim());
    }

    @Override
    @Transactional
    public void updateProcessResult(Long id, AppealHandleRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkCommittee(loginUser, "修改处理结果");
        Appeal appeal = getById(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 3) {
            throw new BusinessException("仅已答复诉求可修改处理结果");
        }
        appeal.setProcessResult(request.getProcessResult());
        appealMapper.updateById(appeal);
        saveFlow(id, 13, loginUser.getUserId(), "修改处理结果：" + request.getProcessResult());
    }

    @Override
    @Transactional
    public void urge(Long id, AppealUrgeRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || !loginUser.getRoles().contains("COMMITTEE")) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅村委工作人员可执行催办操作");
        }
        Appeal appeal = getById(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 2) {
            throw new BusinessException("仅处理中诉求可催办");
        }
        String comment = request != null && StringUtils.hasText(request.getComment())
                ? request.getComment().trim() : "请尽快处理";
        saveFlow(id, 14, loginUser.getUserId(), "催办：" + comment);
    }

    @Override
    @Transactional
    public void satisfied(Long id) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkVillagerFeedbackAppeal(id, loginUser);
        appealMapper.update(null, new LambdaUpdateWrapper<Appeal>()
                .eq(Appeal::getId, id)
                .set(Appeal::getSatisfaction, 1)
                .set(Appeal::getSatisfactionComment, null)
                .set(Appeal::getUpdateBy, loginUser.getUserId())
                .set(Appeal::getUpdateTime, LocalDateTime.now()));
        saveFlow(id, 15, loginUser.getUserId(), "满意");
    }

    @Override
    @Transactional
    public void dissatisfied(Long id, AppealDissatisfiedRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkVillagerFeedbackAppeal(id, loginUser);
        String reason = request.getReason().trim();
        appealMapper.update(null, new LambdaUpdateWrapper<Appeal>()
                .eq(Appeal::getId, id)
                .set(Appeal::getSatisfaction, 3)
                .set(Appeal::getSatisfactionComment, reason)
                .set(Appeal::getStatus, 2)
                .set(Appeal::getFinishTime, null)
                .set(Appeal::getUpdateBy, loginUser.getUserId())
                .set(Appeal::getUpdateTime, LocalDateTime.now()));
        saveFlow(id, 16, loginUser.getUserId(), "不满意：" + reason);
    }

    @Override
    @Transactional
    public void followUp(Long id, AppealFollowUpRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkVillagerFeedbackAppeal(id, loginUser);
        String question = request.getQuestion().trim();
        appealMapper.update(null, new LambdaUpdateWrapper<Appeal>()
                .eq(Appeal::getId, id)
                .set(Appeal::getSatisfaction, 0)
                .set(Appeal::getSatisfactionComment, null)
                .set(Appeal::getStatus, 2)
                .set(Appeal::getFinishTime, null)
                .set(Appeal::getUpdateBy, loginUser.getUserId())
                .set(Appeal::getUpdateTime, LocalDateTime.now()));
        saveFlow(id, 17, loginUser.getUserId(), "追问：" + question);
    }

    /** 校验当前用户是诉求提交村民，且诉求已反馈（已办结）。 */
    private void checkVillagerFeedbackAppeal(Long id, LoginUser loginUser) {
        if (loginUser == null || !loginUser.getRoles().contains("VILLAGER")) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅村民可反馈诉求");
        }
        Appeal appeal = getById(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 4) {
            throw new BusinessException("诉求尚未反馈，暂不可操作");
        }
        if (appeal.getVillagerId() != null && !Objects.equals(appeal.getVillagerId(), loginUser.getVillagerId())) {
            throw new BusinessException("只能反馈本人提交的诉求");
        }
    }

    /** 审核类操作仅村委工作人员与管理员可执行 */
    private void checkCommittee(LoginUser loginUser, String action) {
        if (loginUser == null || (!loginUser.isAdmin() && !loginUser.getRoles().contains("COMMITTEE"))) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅村委工作人员可执行" + action + "操作");
        }
    }

    /**
     * 校验当前用户是网格员且该诉求处于允许操作的状态。
     * 诉求已分派给他人的，不允许越权操作；未指定处理人的允许本网格网格员接手。
     */
    private Appeal getGridWorkerAppeal(Long id, LoginUser loginUser, String action, int... allowedStatuses) {
        if (loginUser == null || !loginUser.getRoles().contains("GRID_WORKER")) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅网格员可执行" + action + "操作");
        }
        Appeal appeal = getById(id);
        boolean allowed = false;
        if (appeal.getStatus() != null) {
            for (int status : allowedStatuses) {
                if (appeal.getStatus() == status) {
                    allowed = true;
                    break;
                }
            }
        }
        if (!allowed) {
            throw new BusinessException("当前状态不可执行" + action + "操作");
        }
        if (appeal.getProcessorId() != null && !Objects.equals(appeal.getProcessorId(), loginUser.getUserId())) {
            throw new BusinessException("该诉求未分派给您，无法执行" + action + "操作");
        }
        return appeal;
    }

    /**
     * 将诉求退回村委待办池：状态恢复为目标状态，清空处理人，并记录流转。
     */
    private void transferBack(Appeal appeal, int operateType, Long operatorId, String comment, int targetStatus) {
        appealMapper.update(null, new LambdaUpdateWrapper<Appeal>()
                .eq(Appeal::getId, appeal.getId())
                .set(Appeal::getStatus, targetStatus)
                .set(Appeal::getProcessorId, null)
                .set(Appeal::getUpdateBy, operatorId)
                .set(Appeal::getUpdateTime, LocalDateTime.now()));
        saveFlow(appeal.getId(), operateType, operatorId, comment);
    }

    private Appeal getById(Long id) {
        Appeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "诉求不存在");
        }
        return appeal;
    }

    private void saveFlow(Long appealId, int operateType, Long operatorId, String comment) {
        AppealFlow flow = new AppealFlow();
        flow.setAppealId(appealId);
        flow.setOperateType(operateType);
        flow.setOperatorId(operatorId);
        flow.setOperateTime(LocalDateTime.now());
        flow.setOperateComment(comment);
        appealFlowMapper.insert(flow);
    }

    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(NO_FORMATTER) + (int) (Math.random() * 1000);
    }
}
