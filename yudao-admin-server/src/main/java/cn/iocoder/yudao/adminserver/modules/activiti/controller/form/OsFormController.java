package cn.iocoder.yudao.adminserver.modules.activiti.controller.form;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.form.OsFormConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.OsFormDO;
import cn.iocoder.yudao.adminserver.modules.activiti.service.form.OsFormService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;



@Api(tags = "动态表单")
@RestController
@RequestMapping("/os/form")
@Validated
public class OsFormController {

    @Resource
    private OsFormService formService;

    @PostMapping("/create")
    @ApiOperation("创建动态表单")
    @PreAuthorize("@ss.hasPermission('os:form:create')")
    public CommonResult<Long> createForm(@Valid @RequestBody OsFormCreateReqVO createReqVO) {
        return success(formService.createForm(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新动态表单")
    @PreAuthorize("@ss.hasPermission('os:form:update')")
    public CommonResult<Boolean> updateForm(@Valid @RequestBody OsFormUpdateReqVO updateReqVO) {
        formService.updateForm(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除动态表单")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('os:form:delete')")
    public CommonResult<Boolean> deleteForm(@RequestParam("id") Long id) {
        formService.deleteForm(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得动态表单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('os:form:query')")
    public CommonResult<OsFormRespVO> getForm(@RequestParam("id") Long id) {
        OsFormDO form = formService.getForm(id);
        return success(OsFormConvert.INSTANCE.convert(form));
    }

    @GetMapping("/list")
    @ApiOperation("获得动态表单列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('os:form:query')")
    public CommonResult<List<OsFormRespVO>> getFormList(@RequestParam("ids") Collection<Long> ids) {
        List<OsFormDO> list = formService.getFormList(ids);
        return success(OsFormConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得动态表单分页")
    @PreAuthorize("@ss.hasPermission('os:form:query')")
    public CommonResult<PageResult<OsFormRespVO>> getFormPage(@Valid OsFormPageReqVO pageVO) {
        PageResult<OsFormDO> pageResult = formService.getFormPage(pageVO);
        return success(OsFormConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出动态表单 Excel")
    @PreAuthorize("@ss.hasPermission('os:form:export')")
    @OperateLog(type = EXPORT)
    public void exportFormExcel(@Valid OsFormExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OsFormDO> list = formService.getFormList(exportReqVO);
        // 导出 Excel
        List<OsFormExcelVO> datas = OsFormConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "动态表单.xls", "数据", OsFormExcelVO.class, datas);
    }

}
