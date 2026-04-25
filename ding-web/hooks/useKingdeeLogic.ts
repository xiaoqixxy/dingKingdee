import { useState } from 'react';
import { API_KINGDEE, DEFAULT_KINGDEE_CONFIG, MOCK_FORM_LIST, OPERATORS, QUICK_DATE_OPTIONS } from '../config/api.config';
import { 
  KingdeeBaseParams, FormItem, SheetField, FilterCondition, 
  SortConfig, SheetMetaRequestParams 
} from '../types';

export const useKingdeeLogic = () => {
  // 基础配置状态
  const [kingdeeConfig, setKingdeeConfig] = useState<KingdeeBaseParams>(DEFAULT_KINGDEE_CONFIG);
  // 流程状态
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(false);
  // 表单列表状态
  const [formList, setFormList] = useState<FormItem[]>([]);
  const [selectedFormId, setSelectedFormId] = useState<string>('');
  const [selectedFormName, setSelectedFormName] = useState<string>('');
  const [formLoading, setFormLoading] = useState<boolean>(false);
  // 字段列表状态
  const [sheetFields, setSheetFields] = useState<SheetField[]>([]);
  const [fieldLoading, setFieldLoading] = useState<boolean>(false);
  // 筛选/排序配置
  const [filterConditions, setFilterConditions] = useState<FilterCondition[]>([{ fieldId: '', operator: '=', value: '' }]);
  const [sortConfigs, setSortConfigs] = useState<SortConfig[]>([{ fieldId: '', order: 'asc' }]);

  // 加载表单列表（模拟数据，实际项目替换为接口请求）
  const loadFormList = async () => {
    setFormLoading(true);
    try {
      // 实际项目中替换为接口请求
      setFormList(MOCK_FORM_LIST);
      if (MOCK_FORM_LIST.length > 0 && !selectedFormId) {
        setSelectedFormId(MOCK_FORM_LIST[0].id);
        setSelectedFormName(MOCK_FORM_LIST[0].name);
      }
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : '加载表单列表失败';
      console.error('加载表单列表失败：', errMsg);
      alert(`加载表单列表失败：${errMsg}`);
      setFormList([]);
    } finally {
      setFormLoading(false);
    }
  };

  // 金蝶登录接口
  const loginToK3Cloud = async (params: KingdeeBaseParams) => {
    if (!params) throw new Error('金蝶登录参数不能为空');
    const requestData = {
      SERVER_URL: params.SERVER_URL.trim(),
      CID: params.CID.trim(),
      USER_NAME: params.USER_NAME.trim(),
      APP_ID: params.APP_ID.trim(),
      APP_SECRET: params.APP_SECRET.trim(),
    };

    try {
      const response = await fetch(API_KINGDEE.login, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData),
        mode: 'cors',
      });

      const result = await response.json();
      if (result?.code === 0) {
        console.log('金蝶登录成功：', result.data);
        return result.data;
      } else {
        throw new Error(result?.msg || '金蝶登录请求失败');
      }
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : '金蝶登录接口调用失败';
      console.error('金蝶登录接口调用失败：', errMsg);
      throw new Error(errMsg);
    }
  };

  // 加载表单字段接口
  const loadSheetMeta = async (params: SheetMetaRequestParams) => {
    setFieldLoading(true);
    try {
      if (!params) throw new Error('表单元数据请求参数不能为空');
      if (!params.selectedFormId) throw new Error('表单ID不能为空');

      const requestData = {
        context: {
          corpId: 'dingbf492c95f9a6eab9acaaa37764f94726',
          unionId: 'JXBKf9sL0GzZE9Qr3bJYvQiEiE',
        },
        params: JSON.stringify(params),
        requestId: `0b830cc8${Date.now().toString(16)}`,
      };

      const response = await fetch(API_KINGDEE.sheetMeta, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Accept': 'application/json',
        },
        body: JSON.stringify(requestData),
        mode: 'cors',
      });

      if (!response.ok) throw new Error(`接口请求失败：${response.status}`);

      const result = await response.json();
      const fields = result?.data?.fields || [];
      setSheetFields([...fields]);

    } catch (error) {
      const errMsg = error instanceof Error ? error.message : '加载表单字段失败';
      console.error('加载表单字段失败：', errMsg);
      alert(`加载失败：${errMsg}`);
      setSheetFields([]);
    } finally {
      setFieldLoading(false);
    }
  };

  // 日期快速选择处理
  const handleQuickDateSelect = (index: number, label: string) => {
    const newFilters = [...filterConditions];
    newFilters[index].value = label;
    setFilterConditions(newFilters);
  };

  // 单选/多选下拉选择处理
  const handleSelectChange = (index: number, value: string | string[]) => {
    const newFilters = [...filterConditions];
    newFilters[index].value = Array.isArray(value) ? value.join(',') : value;
    setFilterConditions(newFilters);
  };

  // 筛选条件操作
  const addFilterCondition = () => {
    setFilterConditions([...filterConditions, { fieldId: '', operator: '=', value: '' }]);
  };

  const removeFilterCondition = (index: number) => {
    if (filterConditions.length <= 1) return;
    const newFilters = [...filterConditions];
    newFilters.splice(index, 1);
    setFilterConditions(newFilters);
  };

  const updateFilterCondition = (index: number, key: keyof FilterCondition, value: string) => {
    const newFilters = [...filterConditions];
    if (newFilters[index]) {
      newFilters[index][key] = value;
      setFilterConditions(newFilters);
    }
  };

  // 排序条件操作
  const addSortConfig = () => {
    setSortConfigs([...sortConfigs, { fieldId: '', order: 'asc' }]);
  };

  const removeSortConfig = (index: number) => {
    if (sortConfigs.length <= 1) return;
    const newSorts = [...sortConfigs];
    newSorts.splice(index, 1);
    setSortConfigs(newSorts);
  };

  const updateSortConfig = (index: number, key: keyof SortConfig, value: string) => {
    const newSorts = [...sortConfigs];
    if (newSorts[index]) {
      newSorts[index][key] = value as 'asc' | 'desc';
      setSortConfigs(newSorts);
    }
  };

  // 步骤切换
  const handleStep1Next = async () => {
    if (!Object.values(kingdeeConfig).every(v => v.trim())) {
      alert('请填写完整的金蝶配置信息！');
      return;
    }

    try {
      setLoading(true);
      // 注释登录接口（测试用）
      // await loginToK3Cloud(kingdeeConfig);
      setCurrentStep(2);
      await loadFormList();
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : '操作失败';
      alert(`操作失败: ${errMsg}`);
    } finally {
      setLoading(false);
    }
  };

  const handleStep2Next = async () => {
    if (!selectedFormId) {
      alert('请选择同步表单！');
      return;
    }

    try {
      setLoading(true);
      const params: SheetMetaRequestParams = {
        ...kingdeeConfig,
        selectedFormId,
        selectedFormName,
      };
      await loadSheetMeta(params);
      setCurrentStep(3);
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : '操作失败';
      alert(`操作失败: ${errMsg}`);
    } finally {
      setLoading(false);
    }
  };

  // 提交配置
  const handleSubmit = async () => {
    if (!selectedFormId || sheetFields.length === 0) {
      alert('核心配置缺失或暂无字段数据，无法提交！');
      return;
    }

    const validFilters = filterConditions.filter(f => f.fieldId.trim() && f.operator.trim());
    const validSorts = sortConfigs.filter(s => s.fieldId.trim());

    try {
      setLoading(true);
      const submitData = {
        ...kingdeeConfig,
        selectedFormId,
        selectedFormName,
        filterConditions: validFilters,
        sortConfigs: validSorts,
        sheetFields,
      };

      console.log('提交数据：', submitData);
      // 实际提交逻辑
      alert('配置提交成功！');
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : '提交失败';
      alert(`操作失败: ${errMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return {
    // 状态
    kingdeeConfig,
    currentStep,
    loading,
    formList,
    selectedFormId,
    selectedFormName,
    formLoading,
    sheetFields,
    fieldLoading,
    filterConditions,
    sortConfigs,
    // 配置常量
    OPERATORS,
    QUICK_DATE_OPTIONS,
    // 方法
    setKingdeeConfig,
    setSelectedFormId,
    setSelectedFormName,
    handleQuickDateSelect,
    handleSelectChange,
    addFilterCondition,
    removeFilterCondition,
    updateFilterCondition,
    addSortConfig,
    removeSortConfig,
    updateSortConfig,
    handleStep1Next,
    handleStep2Next,
    handleSubmit,
    setCurrentStep,
  };
};