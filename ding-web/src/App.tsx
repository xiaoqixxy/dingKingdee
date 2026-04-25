import { useEffect, useState, useRef, useCallback } from 'react';
import { initView } from 'dingtalk-docs-cool-app';
import { API } from './config/api';
import './App.css';

interface MultiSelectProps {
  options: { name: string; value: string }[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder?: string;
}

const MultiSelect: React.FC<MultiSelectProps> = ({ options, value, onChange, placeholder = '请选择' }) => {
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggle = (v: string) => {
    if (value.includes(v)) {
      onChange(value.filter(item => item !== v));
    } else {
      onChange([...value, v]);
    }
  };

  const removeTag = (e: React.MouseEvent, v: string) => {
    e.stopPropagation();
    onChange(value.filter(item => item !== v));
  };

  const selectedLabels = options.filter(o => value.includes(o.value)).map(o => o.name);

  return (
    <div ref={wrapperRef} style={{ position: 'relative', width: '200px' }}>
      <div
        style={{
          width: '100%',
          minHeight: '36px',
          padding: '4px 30px 4px 8px',
          border: '1px solid #dcdfe6',
          borderRadius: '4px',
          background: '#fff',
          cursor: 'pointer',
          display: 'flex',
          flexWrap: 'wrap',
          gap: '4px',
          alignItems: 'center',
          fontSize: '14px',
        }}
        onClick={() => setOpen(!open)}
      >
        {value.length === 0 ? (
          <span style={{ color: '#909399', fontSize: '14px' }}>{placeholder}</span>
        ) : (
          selectedLabels.map((label, i) => (
            <span
              key={i}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                padding: '2px 6px',
                background: '#f0f2f5',
                borderRadius: '3px',
                fontSize: '12px',
                maxWidth: '100px',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
            >
              {label}
              <span
                style={{ marginLeft: '4px', cursor: 'pointer', color: '#909399', fontSize: '14px', lineHeight: 1 }}
                onClick={(e) => removeTag(e, value[i])}
              >
                ×
              </span>
            </span>
          ))
        )}
      </div>
      <span style={{ position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none', color: '#909399' }}>
        {open ? '▲' : '▼'}
      </span>
      {open && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          maxHeight: '200px',
          overflowY: 'auto',
          background: '#fff',
          border: '1px solid #dcdfe6',
          borderRadius: '4px',
          boxShadow: '0 2px 12px rgba(0,0,0,0.1)',
          zIndex: 1000,
          marginTop: '4px',
        }}>
          {options.map((opt) => (
            <div
              key={opt.value}
              style={{
                padding: '8px 12px',
                cursor: 'pointer',
                fontSize: '14px',
                background: value.includes(opt.value) ? '#f5f7fa' : '#fff',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
              onClick={() => toggle(opt.value)}
              onMouseEnter={(e) => (e.currentTarget.style.background = '#f5f7fa')}
              onMouseLeave={(e) => (e.currentTarget.style.background = value.includes(opt.value) ? '#f5f7fa' : '#fff')}
            >
              <input
                type="checkbox"
                checked={value.includes(opt.value)}
                onChange={() => toggle(opt.value)}
                style={{ width: '14px', height: '14px', accentColor: '#409EFF' }}
              />
              <span>{opt.name}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

interface PackageOption {
  id: number;
  productName: string;
  productType: number;
  price: number;
  syncFormLimit: number;
  singleSyncLimit: number;
  monthSyncLimit: number;
  productTypeName?: string;
}

interface KingdeeBaseParams {
  SERVER_URL: string;
  CID: string;
  USER_NAME: string;
  APP_ID: string;
  APP_SECRET: string;
}

interface FormItemType {
  id: string;
  name: string;
}

interface SheetField {
  isPrimary: boolean;
  name: string;
  description: string;
  id: string;
  type: 'text' | 'singleSelect' | 'date' | string;
  property?: {
    choices?: { name: string; value: string }[];
    formatter?: string;
  };
}

type FilterConditionValue = string | string[];

interface FilterCondition {
  fieldId: string;
  operator: string;
  value: FilterConditionValue;
}

interface SortConfig {
  fieldId: string;
  order: 'asc' | 'desc';
}

interface ProductInfo {
  id: number;
  productName: string;
  productType: number;
  productCategory: string;
  price: number;
  syncFormLimit: number;
  singleSyncLimit: number;
  monthSyncLimit: number;
  status: number;
  endTime?: string;
}

const DEFAULT_CONFIG: KingdeeBaseParams = {
  SERVER_URL: 'http://1.95.15.142:1001/k3cloud',
  CID: '65a14d62b909ef',
  USER_NAME: 'administrator',
  APP_ID: '307315_3cbJX7FFUpHVw5+Pw+QNyb9u2jTd5orL',
  APP_SECRET: '383335b1f75b4fbdbed9c920d2175a34',
};

const OPERATORS = ['=', '!=', '>', '>=', '<', '<=', '包含', '不包含'];

const styles: Record<string, any> = {
  container: { padding: '20px', maxWidth: '800px', margin: '0 auto' },
  card: { padding: '20px', background: '#fff', borderRadius: '4px', boxShadow: '0 2px 12px rgba(0,0,0,0.1)' },
  title: { textAlign: 'center', marginBottom: '20px', color: '#303133' },
  stepRow: { marginBottom: '30px', textAlign: 'center', display: 'flex', justifyContent: 'space-around' },
  stepItem: (active: boolean) => ({ color: active ? '#409EFF' : '#999', fontWeight: active ? 600 : 400, fontSize: active ? '16px' : '14px' }),
  formItem: { marginBottom: '18px', display: 'flex', alignItems: 'center' },
  label: { width: '120px', flexShrink: 0, color: '#606266', fontSize: '14px' },
  input: { flex: 1, padding: '8px 12px', border: '1px solid #dcdfe6', borderRadius: '4px', fontSize: '14px' },
  select: { flex: 1, padding: '8px 12px', border: '1px solid #dcdfe6', borderRadius: '4px', fontSize: '14px', background: '#fff' },
  button: { padding: '9px 20px', fontSize: '14px', borderRadius: '4px', border: 'none', cursor: 'pointer', marginRight: '10px' },
  primaryBtn: { background: '#409EFF', color: '#fff' },
  defaultBtn: { background: '#fff', border: '1px solid #dcdfe6', color: '#606666' },
  dangerBtn: { background: '#f56c6c', color: '#fff', padding: '5px 10px', fontSize: '12px' },
  btnGroup: { display: 'flex', justifyContent: 'center', marginTop: '30px' },
  sectionTitle: { margin: '20px 0 10px', color: '#303133', fontWeight: 500, fontSize: '16px' },
  conditionRow: { marginBottom: '10px', display: 'flex', gap: '10px', alignItems: 'center' },
  filterSection: { marginBottom: '20px' },
  filterHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' },
  hr: { margin: '20px 0', border: 'none', borderTop: '1px solid #eee' },
  loading: { textAlign: 'center', padding: '20px', color: '#909399' },
  required: { color: '#f56c6c' },
  productCard: { padding: '12px 16px', background: '#f0f9ff', borderRadius: '4px', marginBottom: '20px', border: '1px solid #bae7ff' },
  productTitle: { fontWeight: 600, color: '#303133', fontSize: '15px' },
  productTag: (color: string) => ({ padding: '2px 8px', borderRadius: '3px', fontSize: '12px', color: '#fff', background: color }),
  productInfo: { display: 'flex', gap: '20px', fontSize: '13px', color: '#606266' },
  dialogOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999 },
  dialogContent: { background: '#fff', borderRadius: '4px', width: '90%', maxWidth: '500px', maxHeight: '80vh', overflow: 'hidden', display: 'flex', flexDirection: 'column' },
  dialogHeader: { padding: '16px 20px', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  dialogClose: { background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer', color: '#909399', padding: '0', lineHeight: 1 },
  dialogBody: { padding: '20px', overflowY: 'auto' },
  packageItem: { padding: '12px 16px', background: '#f5f7fa', borderRadius: '4px', marginBottom: '12px', cursor: 'pointer', border: '1px solid #e4e7ed', transition: 'all 0.2s' },
  packageName: { fontWeight: 600, color: '#303133', fontSize: '15px', marginBottom: '4px' },
  packagePrice: { fontSize: '18px', color: '#f56c6c', fontWeight: 600, marginBottom: '4px' },
  packageInfo: { fontSize: '12px', color: '#909399' },
  channelItem: { padding: '12px 16px', background: '#f5f7fa', borderRadius: '4px', marginBottom: '8px', cursor: 'pointer', border: '1px solid #e4e7ed', textAlign: 'center', fontSize: '14px', color: '#303133' },
};

function App() {
  const [kingdeeConfig, setKingdeeConfig] = useState<KingdeeBaseParams>(DEFAULT_CONFIG);
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);

  const [currentProduct, setCurrentProduct] = useState<ProductInfo | null>(null);
  const [productLoading, setProductLoading] = useState(false);
  const [packageOptions, setPackageOptions] = useState<PackageOption[]>([]);
  const [upgradeDialogVisible, setUpgradeDialogVisible] = useState(false);
  const [selectedPackage, setSelectedPackage] = useState<PackageOption | null>(null);
  const [payDialogVisible, setPayDialogVisible] = useState(false);
  const [payInfo, setPayInfo] = useState<{ orderNo: string; qrCode: string; channelName: string; expireTime: string }>({ orderNo: '', qrCode: '', channelName: '', expireTime: '' });
  const [payCountdown, setPayCountdown] = useState('');
  const [payLoading, setPayLoading] = useState(false);
  const [selectedPayChannel, setSelectedPayChannel] = useState('alipay');
  const [channelDialogVisible, setChannelDialogVisible] = useState(false);

  const [formList, setFormList] = useState<FormItemType[]>([]);
  const [formLoading, setFormLoading] = useState(false);
  const [selectedFormId, setSelectedFormId] = useState<string>('');
  const [selectedFormName, setSelectedFormName] = useState<string>('');
  const [isOtherForm, setIsOtherForm] = useState(false);
  const [otherFormName, setOtherFormName] = useState('');
  const [otherFormKey, setOtherFormKey] = useState('');

  const [sheetFields, setSheetFields] = useState<SheetField[]>([]);
  const [fieldLoading, setFieldLoading] = useState(false);
  const [filterConditions, setFilterConditions] = useState<FilterCondition[]>([{ fieldId: '', operator: '=', value: '' as FilterConditionValue }]);
  const [sortConfigs, setSortConfigs] = useState<SortConfig[]>([{ fieldId: '', order: 'asc' }]);

  const getCorpIdFromUrl = (): string => {
    const path = window.location.pathname;
    const parts = path.split('/').filter(Boolean);
    const corpId = parts[parts.length - 1] || '';
    return corpId || 'jxdj';
  };

  const fetchCurrentProduct = async (corpId: string) => {
    if (!corpId) return;
    setProductLoading(true);
    try {
      const response = await fetch(API.product.getByCorpId(corpId), {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        mode: 'cors',
      });
      const result = await response.json();
      console.log('产品信息:', result);
      if (result.code === 200 && result.data) {
        setCurrentProduct(result.data);
      }
    } catch (error) {
      console.error('获取产品信息失败:', error);
    } finally {
      setProductLoading(false);
    }
  };

  const fetchPackageOptions = async () => {
    try {
      const response = await fetch(API.product.options, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        mode: 'cors',
      });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        setPackageOptions(result.data);
      }
    } catch (error) {
      console.error('获取套餐列表失败:', error);
    }
  };

  const handleUpgradeClick = () => {
    if (packageOptions.length === 0) {
      fetchPackageOptions();
    }
    setUpgradeDialogVisible(true);
  };

  const handleSelectPackage = async (pkg: PackageOption) => {
    setSelectedPackage(pkg);
    setUpgradeDialogVisible(false);
    setChannelDialogVisible(true);
  };

  const handleConfirmPay = async (channel: string) => {
    setSelectedPayChannel(channel);
    setChannelDialogVisible(false);
    if (selectedPackage) {
      await createPayment(selectedPackage, channel);
    }
  };

  const createPayment = async (pkg: PackageOption, channel: string) => {
    const corpId = getCorpIdFromUrl() || 'jxdj';
    setPayLoading(true);
    const channelMap: Record<string, string> = { alipay: '支付宝', wechat_pay: '微信支付', ding_pay: '钉钉支付' };
    try {
      const response = await fetch(API.payment.create, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          dingCorpId: corpId,
          productId: pkg.id,
          channel: channel,
        }),
        mode: 'cors',
      });
      const result = await response.json();
      console.log('支付创建结果:', result);
      if (result.code === 200 && result.data) {
        setPayInfo({
          orderNo: result.data.orderNo,
          qrCode: result.data.qrCode,
          channelName: channelMap[channel] || '支付宝',
          expireTime: result.data.expireTime,
        });
        setPayDialogVisible(true);

        const expireTime = new Date(result.data.expireTime).getTime();
        const checkPayStatus = () => {
          const now = Date.now();
          if (expireTime - now <= 0) {
            showMessage('订单已超时取消', 'warning');
            closePayDialog();
            return;
          }
          const diff = expireTime - now;
          const minutes = Math.floor(diff / 60000);
          const seconds = Math.floor((diff % 60000) / 1000);
          setPayCountdown(`${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`);
        };
        checkPayStatus();
        const timer = setInterval(checkPayStatus, 1000);
        (window as any).__payTimer = timer;
      } else {
        showMessage(result.msg || '创建支付订单失败', 'error');
      }
    } catch (error) {
      console.error('创建支付订单失败:', error);
      showMessage('创建支付订单失败', 'error');
    } finally {
      setPayLoading(false);
    }
  };

  const handlePaySuccess = async () => {
    try {
      await fetch(API.payment.handleResult, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderNo: payInfo.orderNo,
          action: 'pay',
        }),
        mode: 'cors',
      });
      showMessage('支付成功，套餐已升级', 'success');
      closePayDialog();
      const corpId = getCorpIdFromUrl();
      if (corpId) {
        fetchCurrentProduct(corpId);
      }
    } catch (error) {
      showMessage('支付回调处理失败', 'error');
    }
  };

  const handlePayClose = async () => {
    try {
      await fetch(API.payment.handleResult, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderNo: payInfo.orderNo,
          action: 'cancel',
        }),
        mode: 'cors',
      });
      showMessage('订单已取消', 'warning');
      closePayDialog();
    } catch (error) {
      showMessage('取消订单失败', 'error');
    }
  };

  const closePayDialog = () => {
    setPayDialogVisible(false);
    if ((window as any).__payTimer) {
      clearInterval((window as any).__payTimer);
      (window as any).__payTimer = null;
    }
    setPayCountdown('');
    setPayInfo({ orderNo: '', qrCode: '', channelName: '', expireTime: '' });
  };

  useEffect(() => {
    const corpId = getCorpIdFromUrl();
    if (corpId) {
      fetchCurrentProduct(corpId);
    }
  }, []);

  useEffect(() => {
    initView({
      onReady: () => {},
      onError: (e) => console.log('钉钉初始化失败：', e),
    });
  }, []);

  const fetchFormList = async () => {
    setFormLoading(true);
    try {
      const response = await fetch(API.tableConfig.options, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        mode: 'cors',
      });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        const list = result.data.map((item: any) => ({
          id: item.formKey,
          name: item.formName,
        }));
        setFormList(list);
        if (list.length > 0 && !selectedFormId) {
          setSelectedFormId(list[0].id);
          setSelectedFormName(list[0].name);
        }
      }
    } catch (error) {
      console.error('获取表单列表失败:', error);
    } finally {
      setFormLoading(false);
    }
  };

  const showMessage = (msg: string, type: 'success' | 'warning' | 'error' = 'success') => {
    const msgDiv = document.createElement('div');
    msgDiv.textContent = msg;
    msgDiv.style.cssText = `
      position: fixed; top: 20px; left: 50%; transform: translateX(-50%);
      padding: 12px 20px; border-radius: 4px; z-index: 9999;
      background: ${type === 'success' ? '#67c23a' : type === 'warning' ? '#e6a23c' : '#f56c6c'};
      color: #fff; font-size: 14px;
    `;
    document.body.appendChild(msgDiv);
    setTimeout(() => msgDiv.remove(), 3000);
  };

  const updateConfig = (key: keyof KingdeeBaseParams, value: string) => {
    setKingdeeConfig({ ...kingdeeConfig, [key]: value });
  };

  const loginToK3Cloud = async () => {
    const requestData = {
      SERVER_URL: kingdeeConfig.SERVER_URL.trim(),
      CID: kingdeeConfig.CID.trim(),
      USER_NAME: kingdeeConfig.USER_NAME.trim(),
      APP_ID: kingdeeConfig.APP_ID.trim(),
      APP_SECRET: kingdeeConfig.APP_SECRET.trim(),
    };

    const response = await fetch(API.login, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestData),
      mode: 'cors',
      credentials: 'omit',
    });

    const result = await response.json();
    if (result?.code === 0) {
      return result.data;
    } else {
      throw new Error(result?.msg || '金蝶登录请求失败');
    }
  };

  const loadSheetMeta = async () => {
    await loadSheetMetaWithForm(selectedFormId, selectedFormName);
  };

  const loadSheetMetaWithForm = async (formId: string, formName: string) => {
    setFieldLoading(true);
    try {
      const paramsObj = {
        ...kingdeeConfig,
        selectedFormId: formId,
        selectedFormName: formName,
      };

      const requestData = {
        context: {
          corpId: 'dingbf492c95f9a6eab9acaaa37764f94726',
          unionId: 'JXBKf9sL0GzZE9Qr3bJYvQiEiE',
        },
        params: JSON.stringify(paramsObj),
        requestId: `0b830cc8${Date.now().toString(16)}`,
      };

      const response = await fetch(API.sheetMeta, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          Accept: 'application/json',
        },
        body: JSON.stringify(requestData),
        mode: 'cors',
        credentials: 'omit',
      });

      const result = await response.json();
      const fields = result?.data?.fields || [];
      setSheetFields([...fields]);
    } catch (error) {
      showMessage('加载表单字段失败', 'error');
      setSheetFields([]);
    } finally {
      setFieldLoading(false);
    }
  };

  const handleStep1Next = async () => {
    if (!Object.values(kingdeeConfig).every((v) => (v || '').trim())) {
      showMessage('请填写完整的金蝶配置信息', 'warning');
      return;
    }

    try {
      setLoading(true);
      await loginToK3Cloud();
      await fetchFormList();
      setCurrentStep(1);
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '登录失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleStep2Next = async () => {
    let formId = selectedFormId;
    let formName = selectedFormName;
    
    if (isOtherForm) {
      if (!otherFormName.trim() || !otherFormKey.trim()) {
        showMessage('请填写表单名称和表单ID', 'warning');
        return;
      }
      formId = otherFormKey.trim();
      formName = otherFormName.trim();
    } else if (!selectedFormId) {
      showMessage('请选择需要同步的表单', 'warning');
      return;
    }

    try {
      setLoading(true);
      await loadSheetMetaWithForm(formId, formName);
      setSelectedFormId(formId);
      setSelectedFormName(formName);
      setIsOtherForm(false);
      setOtherFormName('');
      setOtherFormKey('');
      setCurrentStep(2);
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '操作失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      setLoading(true);
      const validFilters = filterConditions.filter(f => f.fieldId.trim());
      const validSorts = sortConfigs.filter(s => s.fieldId.trim());
      
      const submitData = {
        ...kingdeeConfig,
        selectedFormId,
        selectedFormName,
        filterConditions: validFilters,
        sortConfigs: validSorts,
        sheetFields,
      };

      console.log('提交数据：', submitData);
      if (window.Dingdocs?.base?.host) {
        await (window.Dingdocs.base.host as any).saveConfigAndGoNext(submitData);
        showMessage('配置提交成功');
      } else {
        showMessage('钉钉环境未初始化', 'error');
      }
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '提交失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const addFilter = () => {
    setFilterConditions([...filterConditions, { fieldId: '', operator: '=', value: '' as FilterConditionValue }]);
  };

  const removeFilter = (index: number) => {
    const newFilters = [...filterConditions];
    newFilters.splice(index, 1);
    setFilterConditions(newFilters);
  };

  const updateFilter = (index: number, key: keyof FilterCondition, value: FilterConditionValue) => {
    const newFilters = [...filterConditions];
    (newFilters[index] as any)[key] = value;
    setFilterConditions(newFilters);
  };

  const addSort = () => {
    setSortConfigs([...sortConfigs, { fieldId: '', order: 'asc' }]);
  };

  const removeSort = (index: number) => {
    const newSorts = [...sortConfigs];
    newSorts.splice(index, 1);
    setSortConfigs(newSorts);
  };

  const updateSort = (index: number, key: keyof SortConfig, value: string) => {
    const newSorts = [...sortConfigs];
    newSorts[index][key] = value as 'asc' | 'desc';
    setSortConfigs(newSorts);
  };

  const renderProductInfo = () => {
    if (productLoading) {
      return <div style={styles.loading}>加载产品信息中...</div>;
    }

    if (!currentProduct) {
      return null;
    }

    const formatDate = (dateStr?: string) => {
      if (!dateStr) return '长期有效';
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN');
    };

    return (
      <div style={styles.productCard}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
          <span style={styles.productTitle}>当前产品：{currentProduct.productName}</span>
          <button
            style={{ ...styles.button, ...styles.primaryBtn, padding: '4px 12px', fontSize: '12px' }}
            onClick={handleUpgradeClick}
          >
            升级套餐
          </button>
        </div>
        <div style={styles.productInfo}>
          <span>到期时间：{formatDate(currentProduct.endTime)}</span>
          <span>可绑定表单：{currentProduct.syncFormLimit}个</span>
          <span>单次同步上限：{currentProduct.singleSyncLimit}条</span>
        </div>
      </div>
    );
  };

  const renderUpgradeDialog = () => {
    if (!upgradeDialogVisible) return null;

    const renderPackageList = () => {
      if (packageOptions.length === 0) {
        return <div style={styles.loading}>加载套餐中...</div>;
      }
      return packageOptions.map((pkg) => {
        const typeText = pkg.productTypeName || (pkg.productType === 1 ? '包月' : pkg.productType === 2 ? '包年' : '按量付费');
        return (
          <div
            key={pkg.id}
            style={styles.packageItem}
            onClick={() => handleSelectPackage(pkg)}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
              <span style={styles.packageName}>{pkg.productName}</span>
              <span style={styles.productTag('#409EFF')}>{typeText}</span>
            </div>
            <div style={styles.packagePrice}>{'¥'}{pkg.price}</div>
            <div style={styles.packageInfo}>
              可绑定表单：{pkg.syncFormLimit}个 | 单表单数据同步上限：{pkg.singleSyncLimit}条
            </div>
          </div>
        );
      });
    };

    return (
      <div style={styles.dialogOverlay} onClick={() => setUpgradeDialogVisible(false)}>
        <div style={styles.dialogContent} onClick={(e) => e.stopPropagation()}>
          <div style={styles.dialogHeader}>
            <h3 style={{ margin: 0 }}>选择套餐</h3>
            <button style={styles.dialogClose} onClick={() => setUpgradeDialogVisible(false)}>×</button>
          </div>
          <div style={styles.dialogBody}>
            {renderPackageList()}
          </div>
        </div>
      </div>
    );
  };

  const renderPayDialog = () => {
    if (!payDialogVisible) return null;

    return (
      <div style={styles.dialogOverlay}>
        <div style={{ ...styles.dialogContent, width: '380px' }} onClick={(e) => e.stopPropagation()}>
          <div style={styles.dialogHeader}>
            <h3 style={{ margin: 0 }}>在线支付</h3>
            <button style={styles.dialogClose} onClick={handlePayClose}>×</button>
          </div>
          <div style={{ padding: '20px', textAlign: 'center' }}>
            <p style={{ fontSize: '14px', marginBottom: '8px' }}>订单号：<strong>{payInfo.orderNo}</strong></p>
            <p style={{ fontSize: '14px', marginBottom: '8px', color: '#606266' }}>支付渠道：{payInfo.channelName}</p>
            <img src={payInfo.qrCode} alt="支付二维码" style={{ width: '200px', height: '200px', marginBottom: '16px' }} />
            <p style={{ fontSize: '14px', color: '#909399', marginBottom: '8px' }}>请使用手机扫码支付</p>
            <p style={{ fontSize: '16px', color: '#e6a23c', fontWeight: 'bold' }}>剩余时间：{payCountdown}</p>
            <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
              <button
                style={{ ...styles.button, ...styles.primaryBtn, flex: 1 }}
                onClick={handlePaySuccess}
              >
                模拟支付成功
              </button>
              <button
                style={{ ...styles.button, ...styles.defaultBtn, flex: 1 }}
                onClick={handlePayClose}
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderChannelDialog = () => {
    if (!channelDialogVisible) return null;

    const channelOptions = [
      { value: 'alipay', label: '支付宝' },
      { value: 'wechat_pay', label: '微信支付' },
      { value: 'ding_pay', label: '钉钉支付' },
    ];

    return (
      <div style={styles.dialogOverlay} onClick={() => setChannelDialogVisible(false)}>
        <div style={{ ...styles.dialogContent, width: '300px' }} onClick={(e) => e.stopPropagation()}>
          <div style={styles.dialogHeader}>
            <h3 style={{ margin: 0 }}>选择支付渠道</h3>
            <button style={styles.dialogClose} onClick={() => setChannelDialogVisible(false)}>×</button>
          </div>
          <div style={styles.dialogBody}>
            {channelOptions.map((ch) => (
              <div
                key={ch.value}
                style={styles.channelItem}
                onClick={() => handleConfirmPay(ch.value)}
              >
                {ch.label}
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };

  const renderStep1 = () => (
    <div>
      <div style={styles.formItem}>
        <label style={styles.label}>服务器地址 <span style={styles.required}>*</span></label>
        <input
          type="text"
          style={styles.input}
          value={kingdeeConfig.SERVER_URL}
          onChange={(e) => updateConfig('SERVER_URL', e.target.value)}
          placeholder="https://xxxx/k3cloud"
        />
      </div>
      <div style={styles.formItem}>
        <label style={styles.label}>帐套ID <span style={styles.required}>*</span></label>
        <input
          type="text"
          style={styles.input}
          value={kingdeeConfig.CID}
          onChange={(e) => updateConfig('CID', e.target.value)}
          placeholder="帐套ID"
        />
      </div>
      <div style={styles.formItem}>
        <label style={styles.label}>用户名 <span style={styles.required}>*</span></label>
        <input
          type="text"
          style={styles.input}
          value={kingdeeConfig.USER_NAME}
          onChange={(e) => updateConfig('USER_NAME', e.target.value)}
          placeholder="用户名"
        />
      </div>
      <div style={styles.formItem}>
        <label style={styles.label}>APP ID <span style={styles.required}>*</span></label>
        <input
          type="text"
          style={styles.input}
          value={kingdeeConfig.APP_ID}
          onChange={(e) => updateConfig('APP_ID', e.target.value)}
          placeholder="APP ID"
        />
      </div>
      <div style={styles.formItem}>
        <label style={styles.label}>APP 密钥 <span style={styles.required}>*</span></label>
        <input
          type="password"
          style={styles.input}
          value={kingdeeConfig.APP_SECRET}
          onChange={(e) => updateConfig('APP_SECRET', e.target.value)}
          placeholder="APP 密钥"
        />
      </div>
      <div style={styles.btnGroup}>
        <button
          style={{ ...styles.button, ...styles.primaryBtn }}
          onClick={handleStep1Next}
          disabled={loading}
        >
          {loading ? '处理中...' : '下一步'}
        </button>
      </div>
    </div>
  );

  const renderStep2 = () => (
    <div>
      <div style={styles.formItem}>
        <label style={styles.label}>选择表单 <span style={styles.required}>*</span></label>
        <select
          style={styles.select}
          value={isOtherForm ? '__other__' : selectedFormId}
          onChange={(e) => {
            if (e.target.value === '__other__') {
              setIsOtherForm(true);
              setSelectedFormId('');
              setSelectedFormName('');
            } else {
              setIsOtherForm(false);
              const form = formList.find((f) => f.id === e.target.value);
              setSelectedFormId(e.target.value);
              setSelectedFormName(form?.name || '');
            }
          }}
        >
          <option value="">-- 请选择表单 --</option>
          {formList.map((item) => (
            <option key={item.id} value={item.id}>{item.name}</option>
          ))}
          <option value="__other__">其他表单</option>
        </select>
      </div>
      
      {isOtherForm && (
        <>
          <div style={styles.formItem}>
            <label style={styles.label}>表单名称 <span style={styles.required}>*</span></label>
            <input
              type="text"
              style={styles.input}
              value={otherFormName}
              onChange={(e) => setOtherFormName(e.target.value)}
              placeholder="请输入表单名称"
            />
          </div>
          <div style={styles.formItem}>
            <label style={styles.label}>表单ID <span style={styles.required}>*</span></label>
            <input
              type="text"
              style={styles.input}
              value={otherFormKey}
              onChange={(e) => setOtherFormKey(e.target.value)}
              placeholder="请输入表单ID"
            />
          </div>
        </>
      )}
      
      <div style={styles.btnGroup}>
        <button
          style={{ ...styles.button, ...styles.defaultBtn }}
          onClick={() => setCurrentStep(0)}
        >
          返回
        </button>
        <button
          style={{ ...styles.button, ...styles.primaryBtn }}
          onClick={handleStep2Next}
          disabled={loading}
        >
          {loading ? '处理中...' : '下一步'}
        </button>
      </div>
    </div>
  );

  const renderStep3 = () => {
    if (fieldLoading) {
      return <div style={styles.loading}>加载表单字段中...</div>;
    }

    const getFieldById = (fieldId: string) => sheetFields.find((f) => f.id === fieldId);

    const renderFilterValue = (filter: FilterCondition, index: number) => {
      const field = getFieldById(filter.fieldId);

      if (!field) {
        return (
          <input
            type="text"
            style={{ ...styles.input, width: '150px' }}
            value={filter.value as string}
            onChange={(e) => updateFilter(index, 'value', e.target.value)}
            placeholder="筛选值"
            disabled
          />
        );
      }

      if (field.type === 'date') {
        return (
          <input
            type="date"
            style={{ ...styles.input, width: '180px' }}
            value={filter.value as string}
            onChange={(e) => updateFilter(index, 'value', e.target.value)}
          />
        );
      }

      if (field.type === 'singleSelect' && field.property?.choices) {
        return (
          <MultiSelect
            options={field.property.choices}
            value={Array.isArray(filter.value) ? filter.value : []}
            onChange={(val) => updateFilter(index, 'value', val)}
            placeholder="请选择"
          />
        );
      }

      return (
        <input
          type="text"
          style={{ ...styles.input, width: '150px' }}
          value={filter.value as string}
          onChange={(e) => updateFilter(index, 'value', e.target.value)}
          placeholder="筛选值"
        />
      );
    };

    return (
      <div>
        <div style={styles.filterSection}>
          <div style={styles.filterHeader}>
            <h4 style={styles.sectionTitle}>筛选条件</h4>
            <button
              style={{ ...styles.button, ...styles.defaultBtn, padding: '4px 12px', fontSize: '12px' }}
              onClick={addFilter}
              disabled={sheetFields.length === 0}
            >
              + 添加筛选
            </button>
          </div>
          {filterConditions.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#909399', padding: '10px', fontSize: '14px' }}>
              暂无筛选条件
            </div>
          ) : (
            filterConditions.map((filter, index) => (
              <div key={index} style={styles.conditionRow}>
                <select
                  style={{ ...styles.select, width: '200px' }}
                  value={filter.fieldId}
                  onChange={(e) => {
                    updateFilter(index, 'fieldId', e.target.value);
                    updateFilter(index, 'value', '');
                  }}
                >
                  <option value="">-- 选择字段 --</option>
                  {sheetFields.map((field) => (
                    <option key={field.id} value={field.id}>{field.name}</option>
                  ))}
                </select>
                <select
                  style={{ ...styles.select, width: '100px' }}
                  value={filter.operator}
                  onChange={(e) => updateFilter(index, 'operator', e.target.value)}
                >
                  {OPERATORS.map((op) => (
                    <option key={op} value={op}>{op}</option>
                  ))}
                </select>
                {renderFilterValue(filter, index)}
                <button
                  style={{ ...styles.button, ...styles.dangerBtn }}
                  onClick={() => removeFilter(index)}
                >
                  删除
                </button>
              </div>
            ))
          )}
        </div>

        <div style={styles.filterSection}>
          <div style={styles.filterHeader}>
            <h4 style={styles.sectionTitle}>排序字段</h4>
            <button
              style={{ ...styles.button, ...styles.defaultBtn, padding: '4px 12px', fontSize: '12px' }}
              onClick={addSort}
              disabled={sheetFields.length === 0}
            >
              + 添加排序
            </button>
          </div>
          {sortConfigs.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#909399', padding: '10px', fontSize: '14px' }}>
              暂无排序条件
            </div>
          ) : (
            sortConfigs.map((sort, index) => (
              <div key={index} style={styles.conditionRow}>
                <select
                  style={{ ...styles.select, width: '200px' }}
                  value={sort.fieldId}
                  onChange={(e) => updateSort(index, 'fieldId', e.target.value)}
                >
                  <option value="">-- 选择字段 --</option>
                  {sheetFields.map((field) => (
                    <option key={field.id} value={field.id}>{field.name}</option>
                  ))}
                </select>
                <select
                  style={{ ...styles.select, width: '100px' }}
                  value={sort.order}
                  onChange={(e) => updateSort(index, 'order', e.target.value)}
                >
                  <option value="asc">升序</option>
                  <option value="desc">降序</option>
                </select>
                <button
                  style={{ ...styles.button, ...styles.dangerBtn }}
                  onClick={() => removeSort(index)}
                >
                  删除
                </button>
              </div>
            ))
          )}
        </div>

        <hr style={styles.hr} />
        <div style={styles.btnGroup}>
          <button
            style={{ ...styles.button, ...styles.defaultBtn }}
            onClick={() => setCurrentStep(1)}
          >
            返回
          </button>
          <button
            style={{ ...styles.button, ...styles.primaryBtn }}
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? '提交中...' : '提交配置'}
          </button>
        </div>
      </div>
    );
  };

  return (
    <div style={styles.container}>
      {renderUpgradeDialog()}
      {renderPayDialog()}
      {renderChannelDialog()}
      <div style={styles.card}>
        {renderProductInfo()}
        <h2 style={styles.title}>金蝶K3Cloud 数据源配置</h2>

        <div style={styles.stepRow}>
          <div style={styles.stepItem(currentStep === 0)}>1. 金蝶配置</div>
          <div style={styles.stepItem(currentStep === 1)}>2. 选择表单</div>
          <div style={styles.stepItem(currentStep === 2)}>3. 筛选排序</div>
        </div>

        {currentStep === 0 && renderStep1()}
        {currentStep === 1 && renderStep2()}
        {currentStep === 2 && renderStep3()}
      </div>
    </div>
  );
}

export default App;