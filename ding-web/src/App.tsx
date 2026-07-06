import { useEffect, useState, useRef } from 'react';
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
              <span style={{ marginLeft: '4px', cursor: 'pointer', color: '#909399', fontSize: '14px', lineHeight: 1 }} onClick={(e) => removeTag(e, value[i])}>
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
        <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, maxHeight: '200px', overflowY: 'auto', background: '#fff', border: '1px solid #dcdfe6', borderRadius: '4px', boxShadow: '0 2px 12px rgba(0,0,0,0.1)', zIndex: 1000, marginTop: '4px' }}>
          {options.map((opt) => (
            <div key={opt.value} style={{ padding: '8px 12px', cursor: 'pointer', fontSize: '14px', background: value.includes(opt.value) ? '#f5f7fa' : '#fff', display: 'flex', alignItems: 'center', gap: '8px' }} onClick={() => toggle(opt.value)} onMouseEnter={(e) => (e.currentTarget.style.background = '#f5f7fa')} onMouseLeave={(e) => (e.currentTarget.style.background = value.includes(opt.value) ? '#f5f7fa' : '#fff')}>
              <input type="checkbox" checked={value.includes(opt.value)} onChange={() => toggle(opt.value)} style={{ width: '14px', height: '14px', accentColor: '#409EFF' }} />
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
  property?: { choices?: { name: string; value: string }[]; formatter?: string };
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

interface VoucherConfigItem {
  id?: number;
  voucherName?: string;
  name?: string;
  orderNo?: number;
  serverUrl?: string;
  cid?: string;
  cId?: string;
  userName?: string;
  appId?: string;
  appSecret?: string;
  selectedFormId?: string;
  selectedFormName?: string;
  filterConditions?: FilterCondition[];
  sortConfigs?: SortConfig[];
  sheetFields?: SheetField[];
  createTime?: string;
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
  SERVER_URL: '',
  CID: '',
  USER_NAME: '',
  APP_ID: '',
  APP_SECRET: '',
};

const OPERATORS = ['=', '!=', '>', '>=', '<', '<=', '包含', '不包含'];

const styles: Record<string, any> = {
  container: { width: '800px', height: '620px', margin: '0 auto', boxSizing: 'border-box' as const, overflow: 'hidden' },
  card: {
    padding: '16px 20px',
    background: '#fff',
    borderRadius: '12px',
    boxShadow: '0 4px 16px rgba(22,119,255,0.12)',
    border: '1px solid rgba(22,119,255,0.08)',
    height: '100%',
    boxSizing: 'border-box' as const,
    overflow: 'auto',
    display: 'flex',
    flexDirection: 'column' as const,
  },
  title: { textAlign: 'center', marginBottom: '28px', color: '#1d2939', fontSize: '20px', fontWeight: 700, letterSpacing: '0.3px' },
  stepRow: {
    marginBottom: '12px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '8px 16px',
    background: '#f8fafc',
    borderRadius: '8px',
    border: '1px solid #e5e7eb',
    position: 'relative' as const,
    flexShrink: 0,
  },
  stepItem: (active: boolean) => ({
    color: active ? '#1677ff' : '#9ca3af',
    fontWeight: active ? 700 : 400,
    fontSize: active ? '15px' : '13px',
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    gap: '6px',
    transition: 'all 0.25s',
  }),
  formItem: { marginBottom: '8px', display: 'flex', flexDirection: 'column' as const, gap: '4px' },
  label: { color: '#374151', fontSize: '13px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '5px' },
  labelIcon: { fontSize: '14px', lineHeight: 1 },
  inputWrapper: { display: 'flex', alignItems: 'center', border: '1px solid #d1d5db', borderRadius: '6px', background: '#fff', overflow: 'hidden', transition: 'border-color 0.2s, box-shadow 0.2s' },
  inputIconBox: { width: '34px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '15px', color: '#9ca3af', flexShrink: 0, borderRight: '1px solid #f0f0f0', background: '#fafafa', alignSelf: 'stretch' as const },
  input: {
    flex: 1,
    padding: '7px 10px',
    border: 'none',
    fontSize: '13px',
    color: '#1d2939',
    outline: 'none',
    background: 'transparent',
  },
  select: {
    flex: 1,
    padding: '7px 10px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '13px',
    background: '#fff',
    color: '#1d2939',
    outline: 'none',
    transition: 'border-color 0.2s, box-shadow 0.2s',
  },
  button: { padding: '8px 18px', fontSize: '14px', borderRadius: '4px', border: 'none', cursor: 'pointer', marginRight: '10px', fontWeight: 500, transition: 'all 0.2s' },
  primaryBtn: { background: '#1677ff', color: '#fff' },
  defaultBtn: { background: '#fff', border: '1px solid #d1d5db', color: '#374151' },
  dangerBtn: { background: '#ff4d4f', color: '#fff', padding: '5px 10px', fontSize: '12px', borderRadius: '4px' },
  btnGroup: { display: 'flex', justifyContent: 'center', marginTop: '14px', gap: '12px' },
  sectionTitle: { margin: '0 0 0', color: '#1d2939', fontWeight: 700, fontSize: '14px', display: 'flex', alignItems: 'center', gap: '6px' },
  conditionRow: { marginBottom: '8px', display: 'flex', gap: '8px', alignItems: 'center', background: '#f8fafc', borderRadius: '8px', padding: '8px 10px', border: '1px solid #f0f0f0' },
  filterSection: { marginBottom: '16px', background: '#fff', borderRadius: '10px', border: '1px solid #e5e7eb', padding: '12px 14px' },
  filterHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' },
  hr: { margin: '24px 0', border: 'none', borderTop: '1px solid #e5e7eb' },
  loading: { textAlign: 'center', padding: '20px', color: '#6b7280' },
  required: { color: '#ff4d4f' },
  productCard: {
    padding: '10px 14px',
    background: 'linear-gradient(135deg, #e6f4ff 0%, #f0f8ff 100%)',
    borderRadius: '8px',
    marginBottom: '12px',
    border: '1px solid #91caff',
    flexShrink: 0,
  },
  productTitle: { fontWeight: 700, color: '#1d2939', fontSize: '15px' },
  productTag: (color: string) => ({
    padding: '2px 10px',
    borderRadius: '20px',
    fontSize: '12px',
    color: '#fff',
    background: color,
    fontWeight: 500,
  }),
  productInfo: { display: 'flex', flexWrap: 'wrap' as const, gap: '16px', fontSize: '13px', color: '#6b7280', marginTop: '8px' },
  dialogOverlay: {
    position: 'fixed' as const,
    top: 0, left: 0, right: 0, bottom: 0,
    background: 'rgba(0,0,0,0.45)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 9999,
    backdropFilter: 'blur(2px)',
  },
  dialogContent: {
    background: '#fff',
    borderRadius: '12px',
    width: '90%',
    maxWidth: '500px',
    maxHeight: '80vh',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column' as const,
    boxShadow: '0 20px 60px rgba(22,119,255,0.2)',
  },
  dialogHeader: {
    padding: '18px 24px',
    borderBottom: '1px solid #e5e7eb',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    background: 'linear-gradient(135deg, #f8faff 0%, #fff 100%)',
  },
  dialogClose: {
    background: 'none',
    border: 'none',
    width: '28px',
    height: '28px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '18px',
    cursor: 'pointer',
    color: '#6b7280',
    padding: '0',
    lineHeight: 1,
    transition: 'all 0.2s',
  },
  dialogBody: { padding: '20px 24px', overflowY: 'auto' as const },
  packageItem: {
    padding: '14px 16px',
    background: '#f8fafc',
    borderRadius: '8px',
    marginBottom: '12px',
    cursor: 'pointer',
    border: '1.5px solid #e5e7eb',
    transition: 'all 0.2s',
  },
  packageName: { fontWeight: 600, color: '#1d2939', fontSize: '15px', marginBottom: '4px' },
  packagePrice: { fontSize: '20px', color: '#ff4d4f', fontWeight: 700, marginBottom: '4px' },
  packageInfo: { fontSize: '12px', color: '#9ca3af' },
  channelItem: {
    padding: '14px 16px',
    background: '#f8fafc',
    borderRadius: '8px',
    marginBottom: '10px',
    cursor: 'pointer',
    border: '1.5px solid #e5e7eb',
    textAlign: 'center' as const,
    fontSize: '14px',
    color: '#1d2939',
    fontWeight: 500,
    transition: 'all 0.2s',
  },
  // 套餐页样式
  upgradePage: {
    width: '100%',
    height: '100%',
    background: '#fff',
    borderRadius: '12px',
    boxShadow: '0 4px 16px rgba(22,119,255,0.12)',
    border: '1px solid rgba(22,119,255,0.08)',
    padding: '16px 20px',
    boxSizing: 'border-box' as const,
    display: 'flex',
    flexDirection: 'column' as const,
    overflow: 'hidden',
  },
  upgradeTopBar: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    marginBottom: '10px',
    flexShrink: 0,
  },
  upgradeTitle: {
    margin: 0,
    fontSize: '16px',
    fontWeight: 700,
    color: '#1d2939',
  },
  backBtn: {
    background: 'none',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    padding: '6px 14px',
    fontSize: '14px',
    color: '#374151',
    cursor: 'pointer',
    fontWeight: 500,
    transition: 'all 0.2s',
  },
  planGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr 1fr',
    gap: '16px',
    flex: 1,
    marginTop: '12px',
    overflow: 'hidden',
  },
  planCard: {
    borderRadius: '10px',
    padding: '18px 16px',
    display: 'flex',
    flexDirection: 'column' as const,
    position: 'relative' as const,
    background: '#fff',
    transition: 'box-shadow 0.2s',
    overflow: 'hidden',
  },
  planActiveBadge: {
    position: 'absolute' as const,
    top: '12px',
    right: '12px',
    color: '#fff',
    fontSize: '11px',
    fontWeight: 600,
    padding: '2px 8px',
    borderRadius: '10px',
  },
  planName: {
    fontSize: '17px',
    fontWeight: 800,
    letterSpacing: '0.5px',
  },
  planDesc: {
    fontSize: '11px',
    color: '#9ca3af',
    textAlign: 'center' as const,
    marginTop: '3px',
    lineHeight: 1.4,
    minHeight: '32px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  planFeatures: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '10px',
  },
  planFeatureRow: {
    display: 'flex',
    alignItems: 'center',
  },
  planFooterNotes: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '6px',
  },
  planNote: {
    fontSize: '11px',
    color: '#9ca3af',
    lineHeight: 1.5,
  },
  planPayBtn: {
    width: '100%',
    padding: '10px 0',
    border: 'none',
    borderRadius: '6px',
    color: '#fff',
    fontSize: '13px',
    fontWeight: 700,
    cursor: 'pointer',
    transition: 'opacity 0.2s',
    letterSpacing: '0.5px',
  },
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
  const [selectedPayChannel, setSelectedPayChannel] = useState('alipay');
  const [channelDialogVisible, setChannelDialogVisible] = useState(false);

  const [formList, setFormList] = useState<FormItemType[]>([]);
  const [selectedFormId, setSelectedFormId] = useState<string>('');
  const [selectedFormName, setSelectedFormName] = useState<string>('');
  const [isOtherForm, setIsOtherForm] = useState(false);
  const [otherFormName, setOtherFormName] = useState('');
  const [otherFormKey, setOtherFormKey] = useState('');

  const [sheetFields, setSheetFields] = useState<SheetField[]>([]);
  const [fieldLoading, setFieldLoading] = useState(false);
  const [filterConditions, setFilterConditions] = useState<FilterCondition[]>([{ fieldId: '', operator: '=', value: '' as FilterConditionValue }]);
  const [sortConfigs, setSortConfigs] = useState<SortConfig[]>([{ fieldId: '', order: 'asc' }]);

  const [viewMode, setViewMode] = useState<"list" | "config">("list");
  const [voucherList, setVoucherList] = useState<VoucherConfigItem[]>([]);
  const [editingVoucherId, setEditingVoucherId] = useState<number | null>(null);
  const [syncMode, setSyncMode] = useState(false);
  const [voucherName, setVoucherName] = useState("");
  const [orderNo, setOrderNo] = useState<number | undefined>(undefined);

  const getCorpIdFromUrl = (): string => {
    const path = window.location.pathname;
    const parts = path.split('/').filter(Boolean);
    const corpId = parts[parts.length - 1] || '';
    return corpId || 'jxdj';
  };

  const fetchVoucherList = async () => {
    const corpId = getCorpIdFromUrl();
    if (!corpId) return;
    try {
      const response = await fetch(API.voucherConfig.listByCorpId(corpId), { method: "GET", headers: { "Content-Type": "application/json" }, mode: "cors" });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        setVoucherList(result.data);
      }
    } catch (error) {
      console.error("获取列表失败:", error);
      showMessage("加载失败", "error");
    }
  };

  const handleAddConfig = () => {
    setEditingVoucherId(null);
    setSyncMode(false);
    setVoucherName("");
    setOrderNo(undefined);
    setKingdeeConfig(DEFAULT_CONFIG);
    setSelectedFormId("");
    setSelectedFormName("");
    setSheetFields([]);
    setFilterConditions([{ fieldId: "", operator: "=", value: "" as FilterConditionValue }]);
    setSortConfigs([{ fieldId: "", order: "asc" }]);
    setCurrentStep(0);
    setViewMode("config");
  };

  const handleEditConfig = (item: VoucherConfigItem) => {
    setEditingVoucherId(item.id || null);
    setSyncMode(false);
    setVoucherName(item.voucherName || item.name || "");
    setOrderNo(item.orderNo);
    setKingdeeConfig({
      SERVER_URL: item.serverUrl || "",
      CID: item.cid || item.cId || "",
      USER_NAME: item.userName || "",
      APP_ID: item.appId || "",
      APP_SECRET: item.appSecret || "",
    });
    setSelectedFormId(item.selectedFormId || "");
    setSelectedFormName(item.selectedFormName || "");
    setSheetFields(item.sheetFields || []);
    setFilterConditions(item.filterConditions || [{ fieldId: "", operator: "=", value: "" as FilterConditionValue }]);
    setSortConfigs(item.sortConfigs || [{ fieldId: "", order: "asc" }]);
    setCurrentStep(0);
    setViewMode("config");
  };

  const handleSyncData = async (item: VoucherConfigItem) => {
    setEditingVoucherId(item.id || null);
    setSyncMode(true);
    setVoucherName(item.voucherName || item.name || "");
    setOrderNo(item.orderNo);
    setKingdeeConfig({
      SERVER_URL: item.serverUrl || "",
      CID: item.cid || item.cId || "",
      USER_NAME: item.userName || "",
      APP_ID: item.appId || "",
      APP_SECRET: item.appSecret || "",
    });
    setSelectedFormId(item.selectedFormId || "");
    setSelectedFormName(item.selectedFormName || "");
    setSheetFields(item.sheetFields || []);
    setFilterConditions(item.filterConditions || [{ fieldId: "", operator: "=", value: "" as FilterConditionValue }]);
    setSortConfigs(item.sortConfigs || [{ fieldId: "", order: "asc" }]);
    await fetchFormList();
    setCurrentStep(1);
    setViewMode("config");
  };

  const handleBackToList = () => {
    setViewMode("list");
    fetchVoucherList();
  };

  const updateConfig = (key: keyof KingdeeBaseParams, value: string) => {
    setKingdeeConfig({ ...kingdeeConfig, [key]: value });
  };

  const showMessage = (msg: string, type: 'success' | 'warning' | 'error' = 'success') => {
    const msgDiv = document.createElement('div');
    msgDiv.textContent = msg;
    msgDiv.style.cssText = `position: fixed; top: 20px; left: 50%; transform: translateX(-50%); padding: 12px 20px; border-radius: 4px; z-index: 9999; background: ${type === 'success' ? '#67c23a' : type === 'warning' ? '#e6a23c' : '#f56c6c'}; color: #fff; font-size: 14px;`;
    document.body.appendChild(msgDiv);
    setTimeout(() => msgDiv.remove(), 3000);
  };

  const fetchCurrentProduct = async (corpId: string) => {
    if (!corpId) return;
    setProductLoading(true);
    try {
      const response = await fetch(API.product.getByCorpId(corpId), { method: 'GET', headers: { 'Content-Type': 'application/json' }, mode: 'cors' });
      const result = await response.json();
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
      const response = await fetch(API.product.options, { method: 'GET', headers: { 'Content-Type': 'application/json' }, mode: 'cors' });
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
    const channelMap: Record<string, string> = { alipay: '支付宝', wechat_pay: '微信支付', transfer: '对公付款' };
    try {
      const response = await fetch(API.payment.create, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ dingCorpId: corpId, productId: pkg.id, channel: channel }),
        mode: 'cors',
      });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        setPayInfo({ orderNo: result.data.orderNo, qrCode: result.data.qrCode, channelName: channelMap[channel] || '支付宝', expireTime: result.data.expireTime });
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
    }
  };

  const handlePaySuccess = async () => {
    try {
      await fetch(API.payment.handleResult, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderNo: payInfo.orderNo, action: 'pay' }), mode: 'cors' });
      showMessage('支付成功，套餐已升级', 'success');
      closePayDialog();
      const corpId = getCorpIdFromUrl();
      if (corpId) fetchCurrentProduct(corpId);
    } catch (error) {
      showMessage('支付回调处理失败', 'error');
    }
  };

  const handlePayClose = async () => {
    try {
      await fetch(API.payment.handleResult, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderNo: payInfo.orderNo, action: 'cancel' }), mode: 'cors' });
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
    if (corpId) fetchCurrentProduct(corpId);
  }, []);

  useEffect(() => {
    initView({ onReady: () => {}, onError: (e) => console.log('钉钉初始化失败：', e) });
  }, []);

  useEffect(() => {
    const corpId = getCorpIdFromUrl();
    if (corpId && viewMode === "list") {
      fetchVoucherList();
    }
  }, [viewMode]);

  const fetchFormList = async () => {
    try {
      const response = await fetch(API.tableConfig.options, { method: 'GET', headers: { 'Content-Type': 'application/json' }, mode: 'cors' });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        const list = result.data.map((item: any) => ({ id: item.formKey, name: item.formName }));
        setFormList(list);
        if (list.length > 0 && !selectedFormId) {
          setSelectedFormId(list[0].id);
          setSelectedFormName(list[0].name);
        }
      }
    } catch (error) {
      console.error('获取表单列表失败:', error);
    }
  };

  const loginToK3Cloud = async () => {
    const requestData = { SERVER_URL: kingdeeConfig.SERVER_URL.trim(), CID: kingdeeConfig.CID.trim(), USER_NAME: kingdeeConfig.USER_NAME.trim(), APP_ID: kingdeeConfig.APP_ID.trim(), APP_SECRET: kingdeeConfig.APP_SECRET.trim() };
    const response = await fetch(API.login, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(requestData), mode: 'cors', credentials: 'omit' });
    const result = await response.json();
    if (result?.code === 0) return result.data;
    throw new Error(result?.msg || '金蝶登录请求失败');
  };

  const loadSheetMetaWithForm = async (formId: string, formName: string) => {
    setFieldLoading(true);
    try {
      const paramsObj = { ...kingdeeConfig, selectedFormId: formId, selectedFormName: formName };
      const requestData = { context: { corpId: 'dingbf492c95f9a6eab9acaaa37764f94726', unionId: 'JXBKf9sL0GzZE9Qr3bJYvQiEiE' }, params: JSON.stringify(paramsObj), requestId: `0b830cc8${Date.now().toString(16)}` };
      const response = await fetch(API.sheetMeta, { method: 'POST', headers: { 'Content-Type': 'application/json; charset=utf-8', Accept: 'application/json' }, body: JSON.stringify(requestData), mode: 'cors', credentials: 'omit' });
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

  const loadSheetMeta = async () => {
    await loadSheetMetaWithForm(selectedFormId, selectedFormName);
  };

  const handleStep1Next = async () => {
    if (!Object.values(kingdeeConfig).every((v) => (v || '').trim())) {
      showMessage('请填写完整的金蝶配置信息', 'warning');
      return;
    }
    if (!voucherName.trim()) {
      showMessage('请填写凭证名称', 'warning');
      return;
    }
    try {
      setLoading(true);
      const validFilters = filterConditions.filter(f => f.fieldId.trim());
      const validSorts = sortConfigs.filter(s => s.fieldId.trim());
      const corpId = getCorpIdFromUrl();
      const submitData: any = {
        voucherName: voucherName.trim(),
        name: voucherName.trim(),
        dingCorpId: corpId,
        orderNo: orderNo,
        serverUrl: kingdeeConfig.SERVER_URL.trim(),
        cid: kingdeeConfig.CID.trim(),
        cId: kingdeeConfig.CID.trim(),
        userName: kingdeeConfig.USER_NAME.trim(),
        appId: kingdeeConfig.APP_ID.trim(),
        appSecret: kingdeeConfig.APP_SECRET.trim(),
        selectedFormId,
        selectedFormName,
        filterConditions: validFilters,
        sortConfigs: validSorts,
        sheetFields,
      };
      if (editingVoucherId !== null) {
        submitData.id = editingVoucherId;
      }
      const response = await fetch(API.voucherConfig.saveOrUpdate, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(submitData),
        mode: 'cors',
      });
      const result = await response.json();
      if (result.code === 200) {
        showMessage('保存成功');
        handleBackToList();
      } else {
        showMessage(result.msg || '保存失败', 'error');
      }
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '保存失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyConfig = async (item: VoucherConfigItem) => {
    try {
      setLoading(true);
      const configForLogin = {
        SERVER_URL: item.serverUrl,
        CID: item.cid || item.cId,
        USER_NAME: item.userName,
        APP_ID: item.appId,
        APP_SECRET: item.appSecret,
      };
      // Temporarily set kingdeeConfig for loginToK3Cloud to use
      const prevConfig = { ...kingdeeConfig };
      Object.keys(configForLogin).forEach((key) => {
        (kingdeeConfig as any)[key] = (configForLogin as any)[key];
      });
      await loginToK3Cloud();
      // Restore original config
      Object.keys(prevConfig).forEach((key) => {
        (kingdeeConfig as any)[key] = (prevConfig as any)[key];
      });
      showMessage('连接成功', 'success');
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '连接失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfig = async (item: VoucherConfigItem) => {
    if (!window.confirm(`确定要删除配置"${item.voucherName || item.name}"吗？此操作不可恢复。`)) return;
    try {
      setLoading(true);
      const response = await fetch(API.voucherConfig.delete(String(item.id)), { method: 'DELETE', headers: { 'Content-Type': 'application/json' }, mode: 'cors' });
      const result = await response.json();
      if (result.code === 200) {
        showMessage('删除成功');
        fetchVoucherList();
      } else {
        showMessage(result.msg || '删除失败', 'error');
      }
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '删除失败', 'error');
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
      const submitData = { ...kingdeeConfig, selectedFormId, selectedFormName, filterConditions: validFilters, sortConfigs: validSorts, sheetFields };
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

  const formatFormLimit = (limit: number, name?: string) => {
    if (!limit || limit <= 0 || limit >= 9999) return '不限制';
    if (name && (name.includes('专业') || name.includes('企业'))) return '不限制';
    return `${limit}个`;
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
    (newSorts[index] as any)[key] = value as 'asc' | 'desc';
    setSortConfigs(newSorts);
  };

  const getPlanLevel = (name?: string): number => {
    if (!name) return 0;
    if (name.includes('企业')) return 3;
    if (name.includes('专业')) return 2;
    if (name.includes('基础')) return 1;
    return 0;
  };

  const getPkgByLevel = (level: number): PackageOption | undefined => {
    const keyword = level === 1 ? '基础' : level === 2 ? '专业' : '企业';
    return packageOptions.find(p => p.productName.includes(keyword));
  };

  const currentLevel = getPlanLevel(currentProduct?.productName);

  const renderPlanFeature = (check: boolean, text: string) => (
    <div style={styles.planFeatureRow}>
      <span style={{ color: check ? '#22c55e' : '#ef4444', fontSize: '14px', marginRight: '6px', fontWeight: 700 }}>
        {check ? '✓' : '✗'}
      </span>
      <span style={{ fontSize: '14px', color: '#374151' }}>{text}</span>
    </div>
  );

  const renderUpgradeDialog = () => {
    if (!upgradeDialogVisible) return null;
    const formatDate = (dateStr?: string) => {
      if (!dateStr) return '长期有效';
      return new Date(dateStr).toLocaleDateString('zh-CN');
    };

    const plans = [
      {
        level: 1,
        name: '基础版',
        desc: '个人或小团队试用，轻量级数据管理。',
        syncLimit: '数据同步上限支持 1000 行',
        form: false,
        report: false,
        footerNotes: ['租户内不限使用人数。', '租户内限制 AI 表格数量为 3 张表。'],
        color: '#6b7280',
        tagColor: '#e5e7eb',
        tagText: '#374151',
      },
      {
        level: 2,
        name: '专业版',
        desc: '成长型团队，日常业务数据分析与管理。',
        syncLimit: '数据同步上限支持 20000 行',
        form: true,
        report: true,
        footerNotes: ['租户内不限使用人数。', '租户内不限 AI 表格数量。'],
        color: '#1677ff',
        tagColor: '#e6f4ff',
        tagText: '#1677ff',
      },
      {
        level: 3,
        name: '企业版',
        desc: '中大型企业，海量数据处理与多部门协作。',
        syncLimit: '数据同步上限支持 50000 行',
        form: true,
        report: true,
        footerNotes: ['租户内不限使用人数。', '租户内不限 AI 表格数量。'],
        color: '#7c3aed',
        tagColor: '#f5f3ff',
        tagText: '#7c3aed',
      },
    ];

    return (
      <div style={styles.upgradePage}>
        {/* 顶部导航 */}
        <div style={styles.upgradeTopBar}>
          <button style={styles.backBtn} onClick={() => setUpgradeDialogVisible(false)}>
            ← 返回上一页
          </button>
          <h2 style={styles.upgradeTitle}>选择套餐</h2>
        </div>

        {/* 当前权益 */}
        {currentProduct && (
          <div style={styles.productCard}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={styles.productTitle}>当前产品：{currentProduct.productName}</span>
            </div>
            <div style={styles.productInfo}>
              <span>到期时间：{formatDate(currentProduct.endTime)}</span>
              <span>可绑定表单：{formatFormLimit(currentProduct.syncFormLimit, currentProduct.productName)}</span>
              <span>单次同步上限：{currentProduct.singleSyncLimit}条</span>
            </div>
          </div>
        )}

        {/* 三列套餐卡片 */}
        <div style={styles.planGrid}>
          {plans.map((plan) => {
            const pkg = getPkgByLevel(plan.level);
            const isActive = currentLevel === plan.level;
            const isDowngrade = currentLevel > plan.level;
            const showPayBtn = !isActive && !isDowngrade;

            return (
              <div
                key={plan.level}
                style={{
                  ...styles.planCard,
                  border: isActive ? `2px solid ${plan.color}` : '2px solid #e5e7eb',
                  boxShadow: isActive ? `0 8px 24px ${plan.color}28` : '0 2px 8px rgba(0,0,0,0.06)',
                }}
              >
                {isActive && (
                  <div style={{ ...styles.planActiveBadge, background: plan.color }}>当前套餐</div>
                )}

                {/* 标题 */}
                <div style={{ textAlign: 'center', marginBottom: '6px' }}>
                  <span style={{ ...styles.planName, color: plan.color }}>{plan.name}</span>
                </div>

                {/* 说明小字 */}
                <div style={styles.planDesc}>{plan.desc}</div>

                <hr style={{ border: 'none', borderTop: '1px solid #f0f0f0', margin: '12px 0 10px' }} />

                {/* 特性列表 */}
                <div style={styles.planFeatures}>
                  {renderPlanFeature(true, plan.syncLimit)}
                  {renderPlanFeature(plan.form, plan.form ? '支持自定义表单' : '不支持自定义表单')}
                  {renderPlanFeature(plan.report, plan.report ? '支持报表' : '不支持报表')}
                </div>

                <hr style={{ border: 'none', borderTop: '1px solid #f0f0f0', margin: '10px 0 8px' }} />

                {/* 底部说明小字 */}
                <div style={styles.planFooterNotes}>
                  {plan.footerNotes.map((note, i) => (
                    <div key={i} style={styles.planNote}>{note}</div>
                  ))}
                </div>

                {/* 支付按钮 */}
                <div style={{ marginTop: 'auto', paddingTop: '10px' }}>
                  {showPayBtn && pkg && (
                    <button
                      style={{ ...styles.planPayBtn, background: plan.color }}
                      onClick={() => handleSelectPackage(pkg)}
                    >
                      支付 ¥{pkg.price}/年
                    </button>
                  )}
                  {isActive && (
                    <div style={{ textAlign: 'center', color: plan.color, fontWeight: 600, fontSize: '14px', padding: '10px 0' }}>
                      ✓ 已开启
                    </div>
                  )}
                  {isDowngrade && (
                    <div style={{ textAlign: 'center', color: '#9ca3af', fontSize: '13px', padding: '10px 0' }}>
                      当前版本更高
                    </div>
                  )}
                  {showPayBtn && !pkg && (
                    <div style={{ textAlign: 'center', color: '#9ca3af', fontSize: '13px', padding: '10px 0' }}>
                      暂无可用套餐
                    </div>
                  )}
                </div>
              </div>
            );
          })}
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
              <button style={{ ...styles.button, ...styles.primaryBtn, flex: 1 }} onClick={handlePaySuccess}>模拟支付成功</button>
              <button style={{ ...styles.button, ...styles.defaultBtn, flex: 1 }} onClick={handlePayClose}>关闭</button>
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
      { value: 'transfer', label: '对公付款', isTransfer: true },
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
              <div key={ch.value} style={{ ...styles.channelItem, background: (ch as any).isTransfer ? '#fef0f0' : '#f5f7fa' }} onClick={() => {
                if ((ch as any).isTransfer) {
                  setChannelDialogVisible(false);
                  window.open('https://qr.dingtalk.com/action/joingroup?code=v1,k1,O7abdQ5/e8+ADgHpaIqTm6ZkxcVyAlWUuOjiQRrCBnGdR7ksupjDEA==&_dt_no_comment=1&origin=11', '_blank');
                } else {
                  handleConfirmPay(ch.value);
                }
              }} title={(ch as any).isTransfer ? '请联系丁江科技（钉钉）' : ''}>
                {ch.label}
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };


  const renderVoucherList = () => (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "auto" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
        <h3 style={{ margin: 0, fontSize: "16px", fontWeight: 700, color: "#1d2939" }}>配置列表</h3>
        <button style={{ ...styles.button, ...styles.primaryBtn, padding: "6px 16px", fontSize: "14px" }} onClick={handleAddConfig}>+ 新增配置</button>
      </div>
      {voucherList.length === 0 ? (
        <div style={{ textAlign: "center", color: "#b0b8c1", padding: "40px", fontSize: "14px", background: "#f8fafc", borderRadius: "8px", border: "1px dashed #e5e7eb" }}>
          暂无配置，点击右上角添加
        </div>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "12px" }}>
          {voucherList.map((item) => (
            <div key={item.id} style={{ padding: "16px", background: "#fff", borderRadius: "8px", border: "1px solid #e5e7eb", boxShadow: "0 2px 8px rgba(0,0,0,0.04)" }}>
              <div style={{ fontWeight: 600, fontSize: "15px", color: "#1d2939", marginBottom: "8px" }}>{item.voucherName || item.name}</div>
              <div style={{ fontSize: "12px", color: "#9ca3af", marginBottom: "12px" }}>服务器地址：{item.serverUrl}</div>
              <div style={{ display: "flex", gap: "8px" }}>
                <button style={{ ...styles.button, background: "#f56c6c", color: "#fff", padding: "4px 12px", fontSize: "12px" }} onClick={() => handleDeleteConfig(item)}>删除</button>
                <button style={{ ...styles.button, background: "#e6a23c", color: "#fff", padding: "4px 12px", fontSize: "12px" }} onClick={() => handleVerifyConfig(item)}>校验</button>
                <button style={{ ...styles.button, ...styles.primaryBtn, padding: "4px 12px", fontSize: "12px" }} onClick={() => handleEditConfig(item)}>编辑</button>
                <button style={{ ...styles.button, background: "#67c23a", color: "#fff", padding: "4px 12px", fontSize: "12px" }} onClick={() => handleSyncData(item)}>同步数据</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  const renderProductInfo = () => {
    if (productLoading) return <div style={styles.loading}>加载产品信息中...</div>;
    if (!currentProduct) return null;
    const formatDate = (dateStr?: string) => {
      if (!dateStr) return '长期有效';
      return new Date(dateStr).toLocaleDateString('zh-CN');
    };
    // formatFormLimit 已提升至组件级别
    return (
      <div style={styles.productCard}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
          <span style={styles.productTitle}>当前产品：{currentProduct.productName}</span>
          <button style={{ ...styles.button, ...styles.primaryBtn, padding: '4px 12px', fontSize: '12px' }} onClick={handleUpgradeClick}>升级套餐</button>
        </div>
        <div style={styles.productInfo}>
          <span>到期时间：{formatDate(currentProduct.endTime)}</span>
          <span>可绑定表单：{formatFormLimit(currentProduct.syncFormLimit, currentProduct.productName)}</span>
          <span>单次同步上限：{currentProduct.singleSyncLimit}条</span>
        </div>
      </div>
    );
  };

  const renderStep1 = () => {
    const fields: { key: keyof KingdeeBaseParams; label: string; icon: string; placeholder: string; type?: string }[] = [
      { key: 'SERVER_URL', label: '服务器地址', icon: '🌐', placeholder: 'https://xxxx/k3cloud' },
      { key: 'CID',        label: '帐套 ID',   icon: '🗂️', placeholder: '请输入帐套 ID' },
      { key: 'USER_NAME',  label: '用户名',     icon: '👤', placeholder: '请输入用户名' },
      { key: 'APP_ID',     label: 'APP ID',     icon: '🔑', placeholder: '请输入 APP ID' },
      { key: 'APP_SECRET', label: 'APP 密钥',   icon: '🔒', placeholder: '请输入 APP 密钥', type: 'password' },
    ];
    return (
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'auto' }}>
        {!syncMode && (
          <div style={{ marginBottom: '12px' }}>
            <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={handleBackToList}>← 返回列表</button>
          </div>
        )}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px 0', padding: '4px 0' }}>
          {fields.map(({ key, label, icon, placeholder, type }) => (
            <div key={key} style={styles.formItem}>
              <label style={styles.label}>
                <span style={styles.labelIcon}>{icon}</span>
                {label} <span style={styles.required}>*</span>
              </label>
              <div style={styles.inputWrapper}>
                <div style={styles.inputIconBox}>{icon}</div>
                <input
                  type={type || 'text'}
                  style={styles.input}
                  value={kingdeeConfig[key]}
                  onChange={(e) => updateConfig(key, e.target.value)}
                  placeholder={placeholder}
                />
              </div>
            </div>
          ))}
        </div>
        <hr style={{ ...styles.hr, margin: "16px 0" }} />
        <div style={styles.formItem}>
          <label style={styles.label}>
            <span style={styles.labelIcon}>馃搵</span>
            凭证名称 <span style={styles.required}>*</span>
          </label>
          <div style={styles.inputWrapper}>
            <div style={styles.inputIconBox}>馃搵</div>
            <input type="text" style={styles.input} value={voucherName} onChange={(e) => setVoucherName(e.target.value)} placeholder="凭证名称" />
          </div>
          <div style={{ fontSize: "11px", color: "#9ca3af", marginTop: "4px" }}></div>
        </div>
        <div style={styles.formItem}>
          <label style={styles.label}>
            <span style={styles.labelIcon}>鈫曪笍</span>
            排序号 <span style={{ color: "#9ca3af", fontWeight: 400 }}>(可选)</span>
          </label>
          <div style={styles.inputWrapper}>
            <div style={styles.inputIconBox}>鈫曪笍</div>
            <input type="number" style={styles.input} value={orderNo ?? ""} onChange={(e) => setOrderNo(e.target.value ? parseInt(e.target.value) : undefined)} placeholder="排序号" />
          </div>
          <div style={{ fontSize: "11px", color: "#9ca3af", marginTop: "4px" }}></div>
        </div>
        <div style={styles.btnGroup}>
          <button style={{ ...styles.button, ...styles.primaryBtn }} onClick={handleStep1Next} disabled={loading}>{loading ? '处理中...' : (syncMode ? '下一步' : '保存')}</button>
        </div>
      </div>
    );
  };

  const renderStep2 = () => (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'auto' }}>
      <div style={styles.formItem}>
        <label style={styles.label}>
          <span style={styles.labelIcon}>📋</span>
          选择表单 <span style={styles.required}>*</span>
        </label>
        <div style={{ ...styles.inputWrapper, padding: 0 }}>
          <div style={styles.inputIconBox}>📋</div>
          <select style={{ ...styles.input, padding: '7px 10px' }} value={isOtherForm ? '__other__' : selectedFormId} onChange={(e) => {
            if (e.target.value === '__other__') {
              setIsOtherForm(true); setSelectedFormId(''); setSelectedFormName('');
            } else {
              setIsOtherForm(false);
              const form = formList.find((f) => f.id === e.target.value);
              setSelectedFormId(e.target.value); setSelectedFormName(form?.name || '');
            }
          }}>
            <option value="">-- 请选择表单 --</option>
            {formList.map((item) => (<option key={item.id} value={item.id}>{item.name}</option>))}
            <option value="__other__">其他表单</option>
          </select>
        </div>
      </div>
      {isOtherForm && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px 0' }}>
          <div style={styles.formItem}>
            <label style={styles.label}><span style={styles.labelIcon}>📝</span>表单名称 <span style={styles.required}>*</span></label>
            <div style={styles.inputWrapper}>
              <div style={styles.inputIconBox}>📝</div>
              <input type="text" style={styles.input} value={otherFormName} onChange={(e) => setOtherFormName(e.target.value)} placeholder="请输入表单名称" />
            </div>
          </div>
          <div style={styles.formItem}>
            <label style={styles.label}><span style={styles.labelIcon}>🔖</span>表单 ID <span style={styles.required}>*</span></label>
            <div style={styles.inputWrapper}>
              <div style={styles.inputIconBox}>🔖</div>
              <input type="text" style={styles.input} value={otherFormKey} onChange={(e) => setOtherFormKey(e.target.value)} placeholder="请输入表单 ID" />
            </div>
          </div>
        </div>
      )}
      <div style={styles.btnGroup}>
        <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={handleBackToList}>返回列表</button>
        <button style={{ ...styles.button, ...styles.primaryBtn }} onClick={handleStep2Next} disabled={loading}>{loading ? '处理中...' : '下一步'}</button>
      </div>
    </div>
  );

  const renderStep3 = () => {
    if (fieldLoading) return <div style={styles.loading}>加载表单字段中...</div>;
    const getFieldById = (fieldId: string) => sheetFields.find((f) => f.id === fieldId);
    const renderFilterValue = (filter: FilterCondition, index: number) => {
      const field = getFieldById(filter.fieldId);
      if (!field) return <input type="text" style={{ ...styles.input, width: '150px' }} value={filter.value as string} onChange={(e) => updateFilter(index, 'value', e.target.value)} placeholder="筛选值" disabled />;
      if (field.type === 'date') return <input type="date" style={{ ...styles.input, width: '180px' }} value={filter.value as string} onChange={(e) => updateFilter(index, 'value', e.target.value)} />;
      if (field.type === 'singleSelect' && field.property?.choices) return <MultiSelect options={field.property.choices} value={Array.isArray(filter.value) ? filter.value : []} onChange={(val) => updateFilter(index, 'value', val)} placeholder="请选择" />;
      return <input type="text" style={{ ...styles.input, width: '150px' }} value={filter.value as string} onChange={(e) => updateFilter(index, 'value', e.target.value)} placeholder="筛选值" />;
    };
    return (
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'auto', paddingRight: '2px' }}>
        <div style={styles.filterSection}>
          <div style={styles.filterHeader}>
            <h4 style={styles.sectionTitle}>🔍 筛选条件</h4>
            <button style={{ ...styles.button, ...styles.defaultBtn, padding: '4px 12px', fontSize: '12px' }} onClick={addFilter} disabled={sheetFields.length === 0}>+ 添加筛选</button>
          </div>
          {filterConditions.length === 0 ? <div style={{ textAlign: 'center', color: '#b0b8c1', padding: '12px', fontSize: '13px', background: '#f8fafc', borderRadius: '6px', border: '1px dashed #e5e7eb' }}>暂无筛选条件，点击右侧按钮添加</div> : filterConditions.map((filter, index) => (
            <div key={index} style={styles.conditionRow}>
              <select style={{ ...styles.select, width: '200px' }} value={filter.fieldId} onChange={(e) => { updateFilter(index, 'fieldId', e.target.value); updateFilter(index, 'value', ''); }}>
                <option value="">-- 选择字段 --</option>
                {sheetFields.map((field) => (<option key={field.id} value={field.id}>{field.name}</option>))}
              </select>
              <select style={{ ...styles.select, width: '100px' }} value={filter.operator} onChange={(e) => updateFilter(index, 'operator', e.target.value)}>
                {OPERATORS.map((op) => (<option key={op} value={op}>{op}</option>))}
              </select>
              {renderFilterValue(filter, index)}
              <button style={{ ...styles.button, ...styles.dangerBtn }} onClick={() => removeFilter(index)}>删除</button>
            </div>
          ))}
        </div>
        <div style={styles.filterSection}>
          <div style={styles.filterHeader}>
            <h4 style={styles.sectionTitle}>↕️ 排序字段</h4>
            <button style={{ ...styles.button, ...styles.defaultBtn, padding: '4px 12px', fontSize: '12px' }} onClick={addSort} disabled={sheetFields.length === 0}>+ 添加排序</button>
          </div>
          {sortConfigs.length === 0 ? <div style={{ textAlign: 'center', color: '#b0b8c1', padding: '12px', fontSize: '13px', background: '#f8fafc', borderRadius: '6px', border: '1px dashed #e5e7eb' }}>暂无排序条件，点击右侧按钮添加</div> : sortConfigs.map((sort, index) => (
            <div key={index} style={styles.conditionRow}>
              <select style={{ ...styles.select, width: '200px' }} value={sort.fieldId} onChange={(e) => updateSort(index, 'fieldId', e.target.value)}>
                <option value="">-- 选择字段 --</option>
                {sheetFields.map((field) => (<option key={field.id} value={field.id}>{field.name}</option>))}
              </select>
              <select style={{ ...styles.select, width: '100px' }} value={sort.order} onChange={(e) => updateSort(index, 'order', e.target.value)}>
                <option value="asc">升序</option>
                <option value="desc">降序</option>
              </select>
              <button style={{ ...styles.button, ...styles.dangerBtn }} onClick={() => removeSort(index)}>删除</button>
            </div>
          ))}
        </div>
        <hr style={{ ...styles.hr, margin: '12px 0' }} />
        <div style={styles.btnGroup}>
          <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={() => setCurrentStep(1)}>返回</button>
          <button style={{ ...styles.button, ...styles.primaryBtn }} onClick={handleSubmit} disabled={loading}>{loading ? '提交中...' : '提交配置'}</button>
        </div>
      </div>
    );
  };

  if (viewMode === "list") {
    return (
      <div style={styles.container}>
        {renderPayDialog()}
        {renderChannelDialog()}
        {upgradeDialogVisible ? (
          renderUpgradeDialog()
        ) : (
          <div style={styles.card}>
            {renderProductInfo()}
            {renderVoucherList()}
          </div>
        )}
      </div>
    );
  }

  return (
    <div style={styles.container}>
      {renderPayDialog()}
      {renderChannelDialog()}
      {upgradeDialogVisible ? (
        renderUpgradeDialog()
      ) : (
        <div style={styles.card}>
          {renderProductInfo()}
          {currentStep === 0 && renderStep1()}
          {currentStep === 1 && renderStep2()}
          {currentStep === 2 && renderStep3()}
        </div>
      )}
    </div>
  );
}

export default App;
