import { useEffect, useState, useRef, useCallback } from 'react';
import { initView } from 'dingtalk-docs-cool-app';
import { API } from './config/api';
import { APP_VERSION } from './config/version';
import './App.css';
import FieldConfigurationStep, { FieldConfig, ReferenceField } from './FieldConfigurationStep';
import { isEmptyOperator, operatorLabels, fieldKind, filterOperators, scalarValue, filterError } from './filterRules';

/** 多选控件的候选项、当前值与变更回调。 */
interface MultiSelectProps {
  options: { name: string; value: string }[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder?: string;
}

/** 多选控件，显示已选标签并允许逐项移除，点击外部关闭候选列表。 */
const MultiSelect: React.FC<MultiSelectProps> = ({ options, value, onChange, placeholder = '请选择' }) => {
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    /** 点击控件外部时关闭下拉，防止浮层残留。 */
    const handleClickOutside = (e: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  /** 切换一个选项的选中状态，通过回调更新受控值。 */
  const toggle = (v: string) => {
    if (value.includes(v)) {
      onChange(value.filter(item => item !== v));
    } else {
      onChange([...value, v]);
    }
  };

  /** 移除标签时阻止冒泡，避免同时切换下拉展开状态。 */
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

/** 可购买套餐及对应价格、限制信息。 */
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

/** 金蝶连接与鉴权参数，只通过既有接口传给服务端。 */
interface KingdeeBaseParams {
  SERVER_URL: string;
  CID: string;
  USER_NAME: string;
  APP_ID: string;
  APP_SECRET: string;
}

/** 可选金蝶业务表单的标识及名称。 */
interface FormItemType {
  id: string;
  name: string;
}

/** 服务端输出字段结构，稳定标识与显示名称分开，引用信息用于逐层配置。 */
interface SheetField {
  isPrimary: boolean;
  name: string;
  description: string;
  id: string;
  type: 'text' | 'singleSelect' | 'date' | string;
  filterType?: string;
  referenceFormId?: string;
  referenceKey?: string;
  property?: { choices?: { name: string; value: string }[]; formatter?: string };
}

type HeaderFillMode = 'FIRST_ROW' | 'ALL_ROWS';
/** 金蝶明细实体选项，携带字段列表、主键和支持状态。 */
interface EntryOption {
  key: string;
  name: string;
  supported: boolean;
  reason?: string;
  fields: SheetField[];
}

type FilterConditionValue = string | string[];

/** 单个字段筛选条件，支持标量与枚举多选值。 */
interface FilterCondition {
  fieldId: string;
  operator: string;
  value: FilterConditionValue;
}

/** 同组条件使用并且连接，多个组之间使用或者连接。 */
interface FilterConditionGroup {
  conditions: FilterCondition[];
}

/** 按稳定输出字段标识保存排序方向。 */
interface SortConfig {
  fieldId: string;
  order: 'asc' | 'desc';
}

/** 服务端保存的连接配置及可恢复的同步选择。 */
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
  selectedEntryKey?: string;
  selectedEntryKeys?: string[];
  headerFillMode?: HeaderFillMode;
  filterConditions?: (FilterCondition | FilterConditionGroup)[];
  sortConfigs?: SortConfig[];
  sheetFields?: SheetField[];
  fieldConfigs?: FieldConfig[];
  createTime?: string;
}

// 同步配置恢复接口由钉钉内嵌宿主提供，当前旧版SDK缺少类型声明；
// 这里兼容宿主接口可能出现的两个层级。
/** 钉钉宿主同步配置读写接口的兼容声明。 */
interface DataSourceHost {
  getSyncConfig?: () => Promise<unknown>;
  saveConfigAndGoNext?: (config: Record<string, unknown>) => Promise<void>;
}
type SavedSyncConfig = Partial<KingdeeBaseParams> & VoucherConfigItem & Record<string, unknown>;

/** 页面提示消息的标识、内容与显示级别。 */
interface ToastItem {
  id: number;
  message: string;
  type: 'success' | 'warning' | 'error';
}

/** 当前企业的产品权益与到期信息。 */
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

// 相对日期只保存稳定键，实际日期区间由服务端按执行时的日期计算。
const DATE_QUICK_SELECTS = [
  { label: '近7天', value: 'last_7_days' },
  { label: '近30天', value: 'last_30_days' },
  { label: '本周', value: 'this_week' },
  { label: '本月', value: 'this_month' },
  { label: '本年', value: 'this_year' },
];

// 组件内联颜色与App.css变量共同定义主题，修改主色时需同步两处，避免不同控件颜色不一致。
const styles: Record<string, any> = {
  container: { width: '750px', maxWidth: '100%', height: '580px', margin: '0 auto', boxSizing: 'border-box' as const, overflow: 'hidden' },
  card: {
    padding: 'var(--plugin-padding)',
    background: '#fff',
    borderRadius: '12px',
    boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
    border: '1px solid #e5e7eb',
    height: '100%',
    boxSizing: 'border-box' as const,
    overflow: 'auto',
    minHeight: 0,
    minWidth: 0,
  },
  title: { textAlign: 'center', marginBottom: '28px', color: '#1d2939', fontSize: '20px', fontWeight: 700, letterSpacing: '0.3px' },
  stepRow: {
    marginBottom: '16px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '12px 20px',
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
  formSelectionPanel: { padding: '16px', border: '1px solid #e5e7eb', borderRadius: '10px', background: '#fff', minWidth: 0 },
  formSelectionHeading: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' },
  formSelectionIcon: { display: 'flex', alignItems: 'center', justifyContent: 'center', width: '40px', height: '40px', flexShrink: 0, borderRadius: '10px', background: '#e6f4ff', fontSize: '21px' },
  formItem: { marginBottom: '14px', display: 'flex', flexDirection: 'column' as const, gap: '8px' },
  label: { color: '#374151', fontSize: '14px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px' },
  labelIcon: { fontSize: '16px', lineHeight: 1 },
  inputWrapper: { display: 'flex', alignItems: 'center', border: '1px solid #d9d9d9', borderRadius: '8px', background: '#fff', overflow: 'hidden', transition: 'border-color 0.2s, box-shadow 0.2s' },
  inputIconBox: { width: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '16px', color: '#bfbfbf', flexShrink: 0, borderRight: '1px solid #f0f0f0', background: '#fafafa', alignSelf: 'stretch' as const },
  input: {
    flex: 1,
    minWidth: 0,
    maxWidth: '100%',
    padding: '9px 12px',
    border: 'none',
    fontSize: '14px',
    color: '#1d2939',
    outline: 'none',
    background: 'transparent',
  },
  select: {
    flex: 1,
    minWidth: 0,
    maxWidth: '100%',
    padding: '9px 12px',
    border: '1px solid #d9d9d9',
    borderRadius: '8px',
    fontSize: '14px',
    background: '#fff',
    color: '#1d2939',
    outline: 'none',
    transition: 'border-color 0.2s, box-shadow 0.2s',
  },
  button: { padding: '10px 24px', fontSize: '14px', borderRadius: '8px', border: 'none', cursor: 'pointer', marginRight: 0, minHeight: '36px', fontWeight: 500, transition: 'all 0.2s' },
  primaryBtn: { background: '#1677ff', color: '#fff' },
  defaultBtn: { background: '#fff', border: '1px solid #d9d9d9', color: '#374151' },
  dangerBtn: { background: '#ff4d4f', color: '#fff', padding: '6px 14px', fontSize: '13px', borderRadius: '6px' },
  btnGroup: { display: 'flex', justifyContent: 'flex-end', flexWrap: 'wrap', marginTop: '16px', gap: '8px', padding: '12px 0 0', borderTop: '1px solid #e5e7eb' },
  sectionTitle: { margin: '0 0 0', color: '#1d2939', fontWeight: 700, fontSize: '15px', display: 'flex', alignItems: 'center', gap: '8px' },
  conditionRow: { marginBottom: '10px', display: 'flex', flexWrap: 'wrap', minWidth: 0, gap: '8px', alignItems: 'center', background: '#f8fafc', borderRadius: '8px', padding: '12px 14px', border: '1px solid #f0f0f0' },
  filterSection: { marginBottom: '20px', background: '#fff', borderRadius: '10px', border: '1px solid #e5e7eb', padding: '12px' },
  filterHeader: { display: 'flex', flexWrap: 'wrap', gap: '8px', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' },
  hr: { margin: '28px 0', border: 'none', borderTop: '1px solid #e5e7eb' },
  loading: { textAlign: 'center', padding: '24px', color: '#6b7280' },
  required: { color: '#ff4d4f' },
  productCard: {
    padding: '14px 18px',
    background: 'linear-gradient(135deg, #e6f4ff 0%, #f0f8ff 100%)',
    borderRadius: '10px',
    marginBottom: '16px',
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
  productInfo: { display: 'flex', flexWrap: 'wrap' as const, gap: '6px 16px', fontSize: '12px', color: '#6b7280', marginTop: '10px' },
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
    maxWidth: 'calc(100vw - 24px)',
    maxHeight: 'calc(100dvh - 24px)',
    overflowY: 'auto',
    flexShrink: 1,
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
    padding: 'var(--plugin-padding)',
    boxSizing: 'border-box' as const,
    display: 'flex',
    flexDirection: 'column' as const,
    overflowY: 'auto',
    minHeight: 0,
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
    gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 200px), 1fr))',
    gap: '16px',
    flexShrink: 0,
    marginTop: '12px',
    paddingBottom: '4px',
  },
  planCard: {
    borderRadius: '10px',
    padding: '40px 14px 14px', minWidth: 0,
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
    minHeight: '64px',
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '10px',
  },
  planFeatureRow: {
    display: 'flex',
    alignItems: 'center',
  },
  planFooterNotes: {
    minHeight: '56px',
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

/** 筛选字段搜索控件，输入仅用于过滤，选择候选项才提交字段标识。 */
function SearchableFieldSelect({ id, fields, value, onChange }: {
  id: string; fields: SheetField[]; value: string; onChange: (value: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [active, setActive] = useState(0);
  const selected = fields.find(field => field.id === value);
  const options = [{ id: '', name: '-- 选择字段 --' }, ...fields.filter(field =>
    `${field.name} ${field.id}`.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()))];
  /** 确认选择候选项并关闭下拉，清空搜索文本。 */
  const choose = (key: string) => {
    onChange(key);
    setOpen(false);
    setSearch('');
  };
  return (
    <div style={{ position: 'relative', width: '200px', maxWidth: '100%' }} onBlur={event => {
      if (!event.currentTarget.contains(event.relatedTarget as Node)) { setOpen(false); setSearch(''); }
    }}>
      <div style={{ ...styles.inputWrapper, padding: 0 }}>
        <input role="combobox" aria-label="筛选字段" aria-autocomplete="list"
          aria-expanded={open} aria-controls={`${id}-options`}
          aria-activedescendant={open ? `${id}-option-${active}` : undefined}
          placeholder="输入字段名称或编码搜索" style={{ ...styles.input, minWidth: 0 }}
          value={open ? search : selected?.name || ''}
          onFocus={() => { setOpen(true); setSearch(''); setActive(0); }}
          onClick={() => { if (!open) { setOpen(true); setSearch(''); setActive(0); } }}
          onChange={event => { setSearch(event.target.value); setOpen(true); setActive(0); }}
          onKeyDown={event => {
            if (event.key === 'Escape') { event.preventDefault(); setOpen(false); setSearch(''); }
            if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
              event.preventDefault(); setOpen(true);
              setActive(index => Math.max(0, Math.min(options.length - 1, index + (event.key === 'ArrowDown' ? 1 : -1))));
            }
            if (event.key === 'Enter' && open) { event.preventDefault(); choose(options[active]?.id || ''); }
          }} />
        <button type="button" aria-label="展开或收起字段列表" aria-expanded={open}
          style={{ background: 'transparent', border: 0, padding: '10px', cursor: 'pointer', color: '#bfbfbf' }}
          onClick={() => { setOpen(!open); setSearch(''); setActive(0); }}>{open ? '▲' : '▼'}</button>
      </div>
      {open && <div id={`${id}-options`} role="listbox" aria-label="筛选字段候选项" style={{
        position: 'absolute', top: '100%', left: 0, right: 0, marginTop: '4px', maxHeight: '200px',
        overflowY: 'auto', background: '#fff', border: '1px solid #d9d9d9', borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.1)', zIndex: 1000,
      }}>
        {options.map((field, index) => <div key={field.id} id={`${id}-option-${index}`}
          role="option" aria-selected={field.id === value}
          onMouseDown={event => event.preventDefault()} onMouseEnter={() => setActive(index)}
          onClick={() => choose(field.id)} style={{ padding: '10px 12px', cursor: 'pointer',
            background: index === active ? '#f3f4f6' : '#fff', fontSize: '13px' }}>{field.name}</div>)}
        {options.length === 1 && search.trim() && <div role="status" style={{ padding: '10px 12px', color: '#9ca3af' }}>暂无匹配字段</div>}
      </div>}
    </div>
  );
}

/** 管理金蝶连接、同步向导、权益套餐和支付界面的主组件。 */
function App() {
  // 连接与向导状态：当前步骤独立于后台连接列表，避免编辑和同步操作混用。
  const [kingdeeConfig, setKingdeeConfig] = useState<KingdeeBaseParams>(DEFAULT_CONFIG);
  const [currentStep, setCurrentStep] = useState(0);
  const [syncConfigLoading, setSyncConfigLoading] = useState(true);
  const [syncConfigError, setSyncConfigError] = useState('');
  const savedSyncConfigRef = useRef<SavedSyncConfig | null>(null);
  const mountedRef = useRef(false);
  const initStartedRef = useRef(false);
  const [loading, setLoading] = useState(false);

  // 套餐和支付状态：套餐页替换主内容区，支付临时状态只用于当前购买流程。
  const [currentProduct, setCurrentProduct] = useState<ProductInfo | null>(null);
  const [productLoading, setProductLoading] = useState(false);
  const [packageOptions, setPackageOptions] = useState<PackageOption[]>([]);
  const [upgradeDialogVisible, setUpgradeDialogVisible] = useState(false);
  const [selectedPackage, setSelectedPackage] = useState<PackageOption | null>(null);
  const [payDialogVisible, setPayDialogVisible] = useState(false);
  const [payInfo, setPayInfo] = useState<{ orderNo: string; qrCode: string; channelName: string; expireTime: string }>({ orderNo: '', qrCode: '', channelName: '', expireTime: '' });
  const [payCountdown, setPayCountdown] = useState('');
  const [channelDialogVisible, setChannelDialogVisible] = useState(false);

  // 表单选择状态：搜索词与真正选中的表单标识分开，防止自由输入被误当作有效表单。
  const [formList, setFormList] = useState<FormItemType[]>([]);
  const [selectedFormId, setSelectedFormId] = useState<string>('');
  const [selectedFormName, setSelectedFormName] = useState<string>('');
  const [isOtherForm, setIsOtherForm] = useState(false);
  const [otherFormName, setOtherFormName] = useState('');
  const [otherFormKey, setOtherFormKey] = useState('');
  const [formSearchText, setFormSearchText] = useState('');
  const [formDropdownOpen, setFormDropdownOpen] = useState(false);
  const [displayValue, setDisplayValue] = useState('');

  // 字段及引用状态：来源配置字段与最终输出字段分开，新增列不能反过来污染基础元数据。
  const [sheetFields, setSheetFields] = useState<SheetField[]>([]);
  const [configurationFields, setConfigurationFields] = useState<SheetField[]>([]);
  const [fieldConfigs, setFieldConfigs] = useState<FieldConfig[]>([]);
  const [fieldConfigBusy, setFieldConfigBusy] = useState(false);
  const referenceMetadataCache = useRef(new Map<string, Promise<ReferenceField[]>>());
  const [headFields, setHeadFields] = useState<SheetField[]>([]);
  const [entryOptions, setEntryOptions] = useState<EntryOption[]>([]);
  const [selectedEntryKey, setSelectedEntryKey] = useState('');
  const [selectedEntryKeys, setSelectedEntryKeys] = useState<string[]>([]);
  const [headerFillMode, setHeaderFillMode] = useState<HeaderFillMode>('FIRST_ROW');
  const [metadataFormId, setMetadataFormId] = useState('');
  const [fieldLoading, setFieldLoading] = useState(false);
  const [toastList, setToastList] = useState<ToastItem[]>([]);
  // 筛选与排序只保存稳定字段标识，显示别名改变后条件仍能关联原列。
  const [filterConditions, setFilterConditions] = useState<FilterConditionGroup[]>([{ conditions: [{ fieldId: '', operator: '=', value: '' as FilterConditionValue }] }]);
  const [sortConfigs, setSortConfigs] = useState<SortConfig[]>([{ fieldId: '', order: 'asc' }]);

  // 连接列表及编辑上下文：退出向导时可以返回原列表，不直接丢失当前连接信息。
  const [viewMode, setViewMode] = useState<"list" | "config">("list");
  const [voucherList, setVoucherList] = useState<VoucherConfigItem[]>([]);
  const [editingVoucherId, setEditingVoucherId] = useState<number | null>(null);
  const [syncMode, setSyncMode] = useState(false);
  const [voucherName, setVoucherName] = useState("");
  const [orderNo, setOrderNo] = useState<number | undefined>(undefined);

  /** 从当前页面路径提取企业标识，独立管理页沿用默认标识。 */
  const getCorpIdFromUrl = (): string => {
    const path = window.location.pathname;
    const parts = path.split('/').filter(Boolean);
    const corpId = parts[parts.length - 1] || '';
    return corpId || 'jxdj';
  };

  /** 按当前企业加载已保存的金蝶连接列表。 */
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

  /** 重置连接表单和历史同步选择，进入新增配置流程。 */
  const handleAddConfig = () => {
    setSelectedEntryKey('');
    setSelectedEntryKeys([]);
    setHeaderFillMode('FIRST_ROW');
    setEntryOptions([]);
    setMetadataFormId('');
    setEditingVoucherId(null);
    setSyncMode(false);
    setVoucherName("");
    setOrderNo(undefined);
    setDisplayValue('');
    setFormSearchText('');
    setFormDropdownOpen(false);
    setIsOtherForm(false);
    setOtherFormKey('');
    setOtherFormName('');
    setHeadFields([]);
    setFieldConfigs([]);
    setConfigurationFields([]);
    setKingdeeConfig(DEFAULT_CONFIG);
    setSelectedFormId("");
    setSelectedFormName("");
    setSheetFields([]);
    setFilterConditions([{ conditions: [{ fieldId: "", operator: "=", value: "" as FilterConditionValue }] }]);
    setSortConfigs([{ fieldId: "", order: "asc" }]);
    setCurrentStep(0);
    setViewMode("config");
  };

  // 兼容早期平铺条件和当前按 OR 分组保存的条件。
  /** 从已有配置恢复表单、明细、字段别名、引用路径及筛选排序。 */
  const restoreSyncSelection = (item: VoucherConfigItem) => {
    setSelectedEntryKey(item.selectedEntryKey || '');
    setSelectedEntryKeys(item.selectedEntryKeys ?? (item.selectedEntryKey ? [item.selectedEntryKey] : []));
    setHeaderFillMode(item.headerFillMode || 'FIRST_ROW');
    setMetadataFormId(item.selectedFormId || '');
    setEntryOptions([]);
    setHeadFields([]);
    setSelectedFormId(item.selectedFormId || '');
    setSelectedFormName(item.selectedFormName || '');
    setDisplayValue(item.selectedFormName || '');
    setFormSearchText('');
    setFormDropdownOpen(false);
    setIsOtherForm(false);
    setOtherFormKey('');
    setOtherFormName('');
    setSheetFields(item.sheetFields || []);
    setFieldConfigs(item.fieldConfigs || []);
    const groups = (item.filterConditions || []).map(condition =>
      'conditions' in condition ? condition : { conditions: [condition] });
    const legacy = item.filterConditions?.every(condition => !('conditions' in condition));
    setFilterConditions(groups.length
      ? legacy ? [{ conditions: item.filterConditions as FilterCondition[] }] : groups
      : [{ conditions: [{ fieldId: '', operator: '=', value: '' }] }]);
    setSortConfigs(item.sortConfigs || [{ fieldId: '', order: 'asc' }]);
  };

  /** 把服务端连接字段映射为前端输入状态，按需打开编辑页。 */
  const handleEditConfig = (item: VoucherConfigItem, open = true) => {
    restoreSyncSelection(item);
    setEditingVoucherId(item.id || null);
    setSyncMode(false);
    setVoucherName(item.voucherName || item.name || '');
    setOrderNo(item.orderNo);
    setKingdeeConfig({
      SERVER_URL: item.serverUrl || '', CID: item.cid || item.cId || '',
      USER_NAME: item.userName || '', APP_ID: item.appId || '', APP_SECRET: item.appSecret || '',
    });
    setCurrentStep(0);
    if (open) setViewMode('config');
  };

  /** 载入选中连接后进入表单选择，保留当前连接的同步设置。 */
  const handleSyncData = async (item: VoucherConfigItem) => {
    handleEditConfig(item, false);
    // 修改已有同步连接时保留当前表的同步范围和字段配置。
    const selection = savedSyncConfigRef.current || item;
    restoreSyncSelection(selection);
    setSyncMode(true);
    await fetchFormList(selection.selectedFormId || '', selection.selectedFormName || '');
    setCurrentStep(1);
    setViewMode('config');
  };

  /** 退出同步向导并返回连接列表，保留需要的配置状态。 */
  const handleBackToList = () => {
    if (syncMode && savedSyncConfigRef.current) {
      savedSyncConfigRef.current = {
        ...savedSyncConfigRef.current, selectedFormId, selectedFormName,
        selectedEntryKey, selectedEntryKeys, headerFillMode, filterConditions, sortConfigs, sheetFields, fieldConfigs,
      };
    }
    setViewMode("list");
    fetchVoucherList();
  };

  /** 更新连接参数中的单个字段。 */
  const updateConfig = (key: keyof KingdeeBaseParams, value: string) => {
    setKingdeeConfig({ ...kingdeeConfig, [key]: value });
  };

  /** 追加具有唯一标识的提示消息并按类型展示。 */
  const showMessage = (msg: string, type: 'success' | 'warning' | 'error' = 'success') => {
    const id = Date.now() + Math.random();
    setToastList(prev => [...prev, { id, message: msg, type }]);
    setTimeout(() => {
      setToastList(prev => prev.filter(t => t.id !== id));
    }, 3000);
  };

  /** 按提示标识移除已关闭的消息。 */
  const removeToast = (id: number) => {
    setToastList(prev => prev.filter(t => t.id !== id));
  };

  /** 读取企业当前产品权益，用于同步限额与套餐展示。 */
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

  /** 加载可购买的套餐列表。 */
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

  /** 切换主内容区到全页套餐选择，不使用叠加弹窗。 */
  const handleUpgradeClick = () => {
    if (packageOptions.length === 0) {
      fetchPackageOptions();
    }
    setUpgradeDialogVisible(true);
  };

  /** 记录选中的套餐并进入支付渠道选择。 */
  const handleSelectPackage = async (pkg: PackageOption) => {
    setSelectedPackage(pkg);
    setUpgradeDialogVisible(false);
    setChannelDialogVisible(true);
  };

  /** 确认渠道后调用支付创建流程。 */
  const handleConfirmPay = async (channel: string) => {
    setChannelDialogVisible(false);
    if (selectedPackage) {
      await createPayment(selectedPackage, channel);
    }
  };

  /** 请求服务端创建订单，并展示二维码及统一到期时间。 */
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
        /** 定时更新支付倒计时与状态，超时后停止轮询。 */
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

  /** 确认支付成功后刷新权益并结束支付展示。 */
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

  /** 处理用户关闭支付界面的既有订单交互。 */
  const handlePayClose = async () => {
    try {
      await fetch(API.payment.handleResult, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderNo: payInfo.orderNo, action: 'cancel' }), mode: 'cors' });
      showMessage('订单已取消', 'warning');
      closePayDialog();
    } catch (error) {
      showMessage('取消订单失败', 'error');
    }
  };

  /** 关闭二维码窗口并清理计时器及支付临时状态。 */
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

  /** 兼容不同钉钉宿主层级，定位支持同步配置读写的接口。 */
  const getDataSourceHost = (): DataSourceHost | undefined => {
    const host = window.Dingdocs?.base?.host as (DataSourceHost & { dataSource?: DataSourceHost }) | undefined;
    return host?.dataSource?.getSyncConfig ? host.dataSource : host;
  };

  /** 从钉钉宿主恢复当前数据源配置，校验格式并防止卸载后更新状态。 */
  const restoreSavedSyncConfig = async (isActive = () => mountedRef.current) => {
    setSyncConfigLoading(true);
    setSyncConfigError('');
    try {
      // 先检测当前宿主是否支持配置恢复；独立浏览器环境不能伪装为钉钉数据源。
      const host = getDataSourceHost();
      if (!host?.getSyncConfig) throw new Error('当前钉钉版本不支持读取同步设置，请更新钉钉后重试');
      const result = await host.getSyncConfig();
      if (!isActive()) return;
      const config = typeof result === 'string' && result.trim() ? JSON.parse(result) : result;
      // 新建数据源尚未保存同步配置，按正常空状态处理。
      if (config == null || config === '' || (typeof config === 'object' && !Array.isArray(config) && Object.keys(config).length === 0)) return;
      if (typeof config !== 'object' || Array.isArray(config) || typeof config.selectedFormId !== 'string' || !config.selectedFormId) {
        throw new Error('原同步设置格式异常，请重试或联系管理员');
      }
      const saved = config as SavedSyncConfig;
      // 保存原始同步配置，后续提交时合并更新，避免覆盖当前界面未管理的历史参数。
      savedSyncConfigRef.current = saved;
      handleEditConfig({
        ...saved,
        serverUrl: saved.SERVER_URL ?? saved.serverUrl,
        cid: saved.CID ?? saved.cid ?? saved.cId,
        userName: saved.USER_NAME ?? saved.userName,
        appId: saved.APP_ID ?? saved.appId,
        appSecret: saved.APP_SECRET ?? saved.appSecret,
      }, false);
      setSyncMode(true);
      await fetchFormList(saved.selectedFormId, saved.selectedFormName || '');
      if (!isActive()) return;
      setCurrentStep(1);
      setViewMode('config');
    } catch (error) {
      if (isActive()) setSyncConfigError('读取原同步设置失败，请重新读取；若仍失败，请更新钉钉后重新打开');
    } finally {
      if (isActive()) setSyncConfigLoading(false);
    }
  };

  useEffect(() => {
    mountedRef.current = true;
    // 宿主初始化只执行一次；开发模式的严格模式会重复执行副作用。
    if (!initStartedRef.current) {
      initStartedRef.current = true;
      initView({
        onReady: () => { if (mountedRef.current) void restoreSavedSyncConfig(); },
        onError: () => {
          if (!mountedRef.current) return;
          setSyncConfigLoading(false);
          // 独立连接管理页没有钉钉宿主，不把该环境误报为已有同步设置损坏。
          if (window.self !== window.top) setSyncConfigError('钉钉初始化失败，请重新打开同步设置');
        },
      });
    }
    return () => { mountedRef.current = false; };
  }, []);

  useEffect(() => {
    /** 点击控件外部时关闭下拉，防止浮层残留。 */
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      // 只有当点击的不是下拉框相关元素时才关闭
      if (!target.closest('[data-form-dropdown]') && formDropdownOpen) {
        setFormDropdownOpen(false);
      }
    };
    if (formDropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [formDropdownOpen]);

  useEffect(() => {
    const corpId = getCorpIdFromUrl();
    if (corpId && viewMode === "list") {
      fetchVoucherList();
    }
  }, [viewMode]);

  /** 读取已配置的金蝶表单选项，并恢复保存的表单选择。 */
  const fetchFormList = async (savedFormId = selectedFormId, savedFormName = selectedFormName) => {
    try {
      const response = await fetch(API.tableConfig.options, { method: 'GET', headers: { 'Content-Type': 'application/json' }, mode: 'cors' });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        const list = result.data.map((item: any) => ({ id: item.formKey, name: item.formName }));
        setFormList(list);
        if (list.length > 0 && !savedFormId) {
          setSelectedFormId(list[0].id);
          setSelectedFormName(list[0].name);
          setDisplayValue(list[0].name);
        } else if (savedFormId && !list.some((form: FormItemType) => form.id === savedFormId)) {
          setIsOtherForm(true);
          setOtherFormKey(savedFormId);
          setOtherFormName(savedFormName);
        }
      }
    } catch (error) {
      console.error('获取表单列表失败:', error);
    }
  };

  /** 用当前连接参数请求服务端校验金蝶登录。 */
  const loginToK3Cloud = async (config: Partial<KingdeeBaseParams> = kingdeeConfig) => {
    const requestData = { SERVER_URL: config.SERVER_URL?.trim() || '', CID: config.CID?.trim() || '', USER_NAME: config.USER_NAME?.trim() || '', APP_ID: config.APP_ID?.trim() || '', APP_SECRET: config.APP_SECRET?.trim() || '' };
    if (Object.values(requestData).some(value => !value)) throw new Error('请完整填写服务器地址、账套 ID、用户名、APP_ID 和 APP_SECRET');
    const response = await fetch(API.login, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(requestData), mode: 'cors', credentials: 'omit' });
    const result = await response.json();
    if (response.ok && result?.code === 0 && result?.data?.verified === true) return result.data;
    if (response.ok && result?.code === 0) throw new Error('服务端尚未返回身份校验结果，请先更新后端');
    throw new Error(result?.msg || '金蝶登录请求失败');
  };

  /** 读取所选表单的表头、可选明细与字段元数据。 */
  const loadSheetMetaWithForm = async (formId: string, formName: string): Promise<SheetField[]> => {
    setFieldLoading(true);
    try {
      const paramsObj = { ...kingdeeConfig, selectedFormId: formId, selectedFormName: formName };
      const requestData = { context: { corpId: 'dingbf492c95f9a6eab9acaaa37764f94726', unionId: 'JXBKf9sL0GzZE9Qr3bJYvQiEiE' }, params: JSON.stringify(paramsObj), requestId: `0b830cc8${Date.now().toString(16)}` };
      const response = await fetch(API.formSchema, { method: 'POST', headers: { 'Content-Type': 'application/json; charset=utf-8', Accept: 'application/json' }, body: JSON.stringify(requestData), mode: 'cors', credentials: 'omit' });
      const result = await response.json();
      if (!response.ok || result?.code !== 0) throw new Error(result?.msg || '加载表单字段失败');
      const fields = result?.data?.fields || [];
      const options: EntryOption[] = result?.data?.entryOptions || [];
      setHeadFields(fields);
      setEntryOptions(options);
      if (formId !== metadataFormId || selectedEntryKeys.some(key => !options.some(option => option.key === key && option.supported))) {
        setSelectedEntryKey('');
        setSelectedEntryKeys([]);
        setHeaderFillMode('FIRST_ROW');
      }
      if (formId !== metadataFormId) {
        setFieldConfigs([]);
        setFilterConditions([{ conditions: [{ fieldId: '', operator: '=', value: '' }] }]);
        setSortConfigs([{ fieldId: '', order: 'asc' }]);
      }
      setMetadataFormId(formId);
      setSheetFields([...fields]);
      return fields;
    } catch (error) {
      showMessage('加载表单字段失败', 'error');
      setSheetFields([]);
      return [];
    } finally {
      setFieldLoading(false);
    }
  };

  /** 校验连接输入并保存当前连接，成功后推进同步向导。 */
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
      const invalid = filterConditions.flatMap(group => group.conditions).find(f => filterError(f, sheetFields.find(field => field.id === f.fieldId)));
      if (invalid) { showMessage(filterError(invalid, sheetFields.find(field => field.id === invalid.fieldId)), 'warning'); return; }
      // 过滤掉空条件,保持嵌套的分组结构
      const validFilters = filterConditions.map(group => ({
        conditions: group.conditions.filter(f => f.fieldId.trim()).map(f => ({ ...f, value: isEmptyOperator(f.operator) ? '' : f.operator === '包含于' ? f.value : scalarValue(f.value) }))
      })).filter(group => group.conditions.length > 0);
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
        selectedEntryKey,
        selectedEntryKeys,
        headerFillMode,
        filterConditions: validFilters,
        sortConfigs: validSorts,
        sheetFields,
        fieldConfigs,
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

  /** 只校验已有连接的登录状态并显示结果。 */
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
      await loginToK3Cloud(configForLogin);
      showMessage('身份与授权校验通过', 'success');
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '连接失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  /** 确认后删除指定连接并刷新列表。 */
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

  /** 校验表单选择并读取元数据，再进入明细范围选择。 */
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
    } else {
      // 非其他表单模式下，必须有有效的 selectedFormId 和 selectedFormName
      if (!selectedFormId || !selectedFormId.trim()) {
        showMessage('请选择需要同步的表单', 'warning');
        return;
      }
      if (!selectedFormName || !selectedFormName.trim()) {
        showMessage('请选择需要同步的表单', 'warning');
        return;
      }
      // 验证选中的表单是否在列表中（防止使用已删除的表单）
      const isValidForm = formList.some(f => f.id === selectedFormId);
      if (!isValidForm) {
        showMessage('所选表单不存在，请重新选择', 'warning');
        return;
      }
    }
    try {
      setLoading(true);
      const fields = await loadSheetMetaWithForm(formId, formName);
      // 未获取到表单元数据字段信息，阻止进入下一步
      if (!fields || fields.length === 0) {
        showMessage('未能获取到表单元数据字段信息，请检查表单配置或网络后重试', 'error');
        return;
      }
      setSelectedFormId(formId);
      setSelectedFormName(formName);
      setDisplayValue(formName);
      setCurrentStep(2);
    } catch (error) {
      showMessage(error instanceof Error ? error.message : '操作失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  /** 校验筛选排序后将完整同步配置提交给钉钉宿主。 */
  const handleSubmit = async () => {
    try {
      setLoading(true);
      const invalid = filterConditions.flatMap(group => group.conditions).find(f => filterError(f, sheetFields.find(field => field.id === f.fieldId)));
      if (invalid) { showMessage(filterError(invalid, sheetFields.find(field => field.id === invalid.fieldId)), 'warning'); return; }
      // 过滤掉空条件,保持嵌套的分组结构
      const validFilters = filterConditions.map(group => ({
        conditions: group.conditions.filter(f => f.fieldId.trim()).map(f => ({ ...f, value: isEmptyOperator(f.operator) ? '' : f.operator === '包含于' ? f.value : scalarValue(f.value) }))
      })).filter(group => group.conditions.length > 0);
      const validSorts = sortConfigs.filter(s => s.fieldId.trim());
      // 沿用当前宿主保存的其他配置，只更新本次向导明确修改的同步参数。
      const submitData = { ...savedSyncConfigRef.current, ...kingdeeConfig, configVersion: 2, selectedFormId, selectedFormName, selectedEntryKey, selectedEntryKeys, headerFillMode, filterConditions: validFilters, sortConfigs: validSorts, sheetFields, fieldConfigs };
      const host = getDataSourceHost();
      if (host?.saveConfigAndGoNext) {
        await host.saveConfigAndGoNext(submitData);
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

  /** 把套餐的绑定表单数量限制转为用户可读文字。 */
  const formatFormLimit = (limit: number, name?: string) => {
    if (!limit || limit <= 0 || limit >= 9999) return '不限制';
    if (name && (name.includes('专业') || name.includes('企业'))) return '不限制';
    return `${limit}个`;
  };

  // 在指定组添加条件(AND关系)
  /** 在指定条件组内追加一个并且条件。 */
  const addFilter = (groupIndex?: number) => {
    setFilterConditions(prev => {
      const newGroups = [...prev];
      const targetIndex = groupIndex !== undefined ? groupIndex : newGroups.length - 1;
      newGroups[targetIndex] = {
        ...newGroups[targetIndex],
        conditions: [...newGroups[targetIndex].conditions, { fieldId: '', operator: '=', value: '' as FilterConditionValue }]
      };
      return newGroups;
    });
  };

  // 创建新的条件组(OR关系)
  /** 添加独立的或者条件组。 */
  const addOr = () => {
    setFilterConditions(prev => [
      ...prev,
      { conditions: [{ fieldId: '', operator: '=', value: '' as FilterConditionValue }] }
    ]);
  };

  // 删除条件或整个组
  /** 移除指定条件并维持可编辑的条件组结构。 */
  const removeFilter = (groupIndex: number, conditionIndex: number) => {
    setFilterConditions(prev => {
      const newGroups = [...prev];
      const group = newGroups[groupIndex];
      
      // 如果组内只有一个条件且不是唯一组,删除整个组
      if (group.conditions.length === 1 && newGroups.length > 1) {
        newGroups.splice(groupIndex, 1);
      } else {
        // 否则只删除该条件
        const newConditions = [...group.conditions];
        newConditions.splice(conditionIndex, 1);
        newGroups[groupIndex] = { ...group, conditions: newConditions };
      }
      
      return newGroups;
    });
  };

  // 更新特定条件
  /** 更新筛选字段、操作符或取值，避免跨类型保留不适用的值。 */
  const updateFilter = (groupIndex: number, conditionIndex: number, key: keyof FilterCondition, value: FilterConditionValue) => {
    setFilterConditions(prev => {
      const newGroups = [...prev];
      const newConditions = [...newGroups[groupIndex].conditions];
      newConditions[conditionIndex] = { ...newConditions[conditionIndex], [key]: value } as FilterCondition;
      newGroups[groupIndex] = { ...newGroups[groupIndex], conditions: newConditions };
      return newGroups;
    });
  };

  /** 增加一个排序配置行。 */
  const addSort = () => {
    setSortConfigs([...sortConfigs, { fieldId: '', order: 'asc' }]);
  };

  /** 移除指定排序项。 */
  const removeSort = (index: number) => {
    const newSorts = [...sortConfigs];
    newSorts.splice(index, 1);
    setSortConfigs(newSorts);
  };

  /** 更新排序字段或方向。 */
  const updateSort = (index: number, key: keyof SortConfig, value: string) => {
    const newSorts = [...sortConfigs];
    newSorts[index] = { ...newSorts[index], [key]: value } as SortConfig;
    setSortConfigs(newSorts);
  };

  /** 将套餐名称映射为页面用于比较的权益等级。 */
  const getPlanLevel = (name?: string): number => {
    if (!name) return 0;
    if (name.includes('企业')) return 3;
    if (name.includes('专业')) return 2;
    if (name.includes('基础')) return 1;
    return 0;
  };

  /** 从套餐选项中寻找指定等级的套餐。 */
  const getPkgByLevel = (level: number): PackageOption | undefined => {
    const keyword = level === 1 ? '基础' : level === 2 ? '专业' : '企业';
    return packageOptions.find(p => p.productName.includes(keyword));
  };

  const currentLevel = getPlanLevel(currentProduct?.productName);

  /** 渲染套餐功能是否包含的对比条目。 */
  const renderPlanFeature = (check: boolean, text: string) => (
    <div style={styles.planFeatureRow}>
      <span style={{ color: check ? '#22c55e' : '#ef4444', fontSize: '14px', marginRight: '6px', fontWeight: 700 }}>
        {check ? '✓' : '✗'}
      </span>
      <span style={{ fontSize: '14px', color: '#374151' }}>{text}</span>
    </div>
  );

  /** 渲染全页套餐选择内容，受升级页面状态控制。 */
  const renderUpgradeDialog = () => {
    if (!upgradeDialogVisible) return null;
    /** 将套餐或权益到期时间格式化为本地显示文本。 */
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
        // report: false,
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
        // report: true,
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
        // report: true,
        footerNotes: ['租户内不限使用人数。', '租户内不限 AI 表格数量。'],
        color: '#7c3aed',
        tagColor: '#f5f3ff',
        tagText: '#7c3aed',
      },
    ];

    return (
      <div className="plugin-page" style={styles.upgradePage}>
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
            <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
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
                  {/*{renderPlanFeature(plan.report, plan.report ? '支持报表' : '不支持报表')}*/}
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

  /** 渲染二维码支付、倒计时和支付操作区域。 */
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

  /** 渲染支付渠道选择窗口。 */
  const renderChannelDialog = () => {
    if (!channelDialogVisible) return null;
    const channelOptions = [
      // { value: 'alipay', label: '支付宝' },
      // { value: 'wechat_pay', label: '微信支付' },
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


  /** 渲染页面消息提示及关闭操作。 */
  const renderToast = () => {
    if (toastList.length === 0) return null;
    const config = {
      success: { bg: '#f0fdf4', border: '#bbf7d0', icon: '✓', iconColor: '#22c55e', textColor: '#15803d', shadow: 'rgba(34,197,94,0.18)' },
      warning: { bg: '#fffbeb', border: '#fde68a', icon: '!', iconColor: '#f59e0b', textColor: '#b45309', shadow: 'rgba(245,158,11,0.18)' },
      error: { bg: '#fef2f2', border: '#fecaca', icon: '✕', iconColor: '#ef4444', textColor: '#b91c1c', shadow: 'rgba(239,68,68,0.18)' },
    };
    return (
      <div style={{ position: 'fixed', top: '20px', left: '50%', transform: 'translateX(-50%)', zIndex: 10000, display: 'flex', flexDirection: 'column', gap: '8px', pointerEvents: 'none' }}>
        {toastList.map(t => {
          const c = config[t.type];
          return (
            <div key={t.id} className="toast-slide-down" style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              background: c.bg,
              border: `1px solid ${c.border}`,
              borderRadius: '10px',
              padding: '14px 20px',
              boxShadow: `0 8px 24px ${c.shadow}, 0 2px 8px rgba(0,0,0,0.06)`,
              minWidth: 0,
              width: 'min(500px, calc(100vw - 24px))',
              maxWidth: '100%',
              pointerEvents: 'auto',
            }}>
              <div style={{
                width: '28px',
                height: '28px',
                borderRadius: '50%',
                background: c.iconColor,
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '15px',
                fontWeight: 700,
                flexShrink: 0,
              }}>{c.icon}</div>
              <span style={{ fontSize: '14px', color: c.textColor, fontWeight: 500, flex: 1, lineHeight: 1.4 }}>{t.message}</span>
              <button
                style={{ background: 'none', border: 'none', color: '#9ca3af', fontSize: '18px', cursor: 'pointer', padding: '0', lineHeight: 1, flexShrink: 0, width: '20px', height: '20px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '4px', transition: 'all 0.2s' }}
                onClick={() => removeToast(t.id)}
                onMouseEnter={(e) => { e.currentTarget.style.color = c.textColor; e.currentTarget.style.background = c.border; }}
                onMouseLeave={(e) => { e.currentTarget.style.color = '#9ca3af'; e.currentTarget.style.background = 'none'; }}
              >×</button>
            </div>
          );
        })}
      </div>
    );
  };

  /** 渲染连接列表及校验、编辑、删除、同步入口。 */
  const renderVoucherList = () => (
    <div style={{ minWidth: 0 }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
        <h3 style={{ margin: 0, fontSize: "16px", fontWeight: 700, color: "#1d2939" }}>配置列表</h3>
        <button style={{ ...styles.button, ...styles.primaryBtn, padding: "6px 16px", fontSize: "14px" }} onClick={handleAddConfig}>+ 新增配置</button>
      </div>
      {voucherList.length === 0 ? (
        <div style={{ textAlign: "center", color: "#b0b8c1", padding: "40px", fontSize: "14px", background: "#f8fafc", borderRadius: "8px", border: "1px dashed #e5e7eb" }}>
          暂无配置，点击右上角添加
        </div>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(2, minmax(0, 1fr))", gap: "12px" }}>
          {voucherList.map((item) => (
            <div key={item.id} style={{ minWidth: 0, display: "flex", flexDirection: "column", padding: "14px", background: "#fff", borderRadius: "8px", border: "1px solid #e5e7eb", boxShadow: "0 2px 8px rgba(0,0,0,0.04)" }}>
              <div style={{ fontWeight: 600, fontSize: "15px", color: "#1d2939", minHeight: "46px", marginBottom: "6px" }}>{item.voucherName || item.name}</div>
              <div style={{ fontSize: "12px", color: "#6b7280", minHeight: "36px", marginBottom: "12px" }}>服务器地址：{item.serverUrl}</div>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginTop: "auto" }}>
                <button style={{ ...styles.button, background: "#f56c6c", color: "#fff", padding: "4px 12px", fontSize: "12px" }} onClick={() => handleDeleteConfig(item)}>删除</button>
                <button style={{ ...styles.button, background: "#e6a23c", color: "#fff", padding: "4px 12px", fontSize: "12px" }} disabled={loading} onClick={() => handleVerifyConfig(item)}>{loading ? '校验中...' : '校验'}</button>
                <button style={{ ...styles.button, ...styles.primaryBtn, padding: "4px 12px", fontSize: "12px" }} onClick={() => handleEditConfig(item)}>编辑</button>
                <button style={{ ...styles.button, background: "#67c23a", color: "#fff", padding: "4px 12px", fontSize: "12px" }} onClick={() => handleSyncData(item)}>同步数据</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  /** 展示当前套餐权益，只在流程前部显示以节省配置空间。 */
  const renderProductInfo = () => {
    if (productLoading) return <div style={styles.loading}>加载产品信息中...</div>;
    if (!currentProduct) return null;
    /** 将套餐或权益到期时间格式化为本地显示文本。 */
    const formatDate = (dateStr?: string) => {
      if (!dateStr) return '长期有效';
      return new Date(dateStr).toLocaleDateString('zh-CN');
    };
    // formatFormLimit 已提升至组件级别
    return (
      <div style={styles.productCard}>
        <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
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

  /** 渲染连接参数单列表单，保持标签和输入图标布局一致。 */
  const renderStep1 = () => {
    const fields: { key: keyof KingdeeBaseParams; label: string; icon: string; placeholder: string; type?: string }[] = [
      { key: 'SERVER_URL', label: '服务器地址', icon: '🌐', placeholder: 'https://xxxx/k3cloud' },
      { key: 'CID',        label: '帐套 ID',   icon: '🗂️', placeholder: '请输入帐套 ID' },
      { key: 'USER_NAME',  label: '用户名',     icon: '👤', placeholder: '请输入用户名' },
      { key: 'APP_ID',     label: 'APP ID',     icon: '🔑', placeholder: '请输入 APP ID' },
      { key: 'APP_SECRET', label: 'APP 密钥',   icon: '🔒', placeholder: '请输入 APP 密钥', type: 'password' },
    ];
    return (
      <div style={{ minWidth: 0, display: 'flex', flexDirection: 'column' }}>
        {!syncMode && (
          <div style={{ marginBottom: '12px' }}>
            <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={handleBackToList}>← 返回列表</button>
          </div>
        )}
        <a
          className="connection-guide"
          href="https://alidocs.dingtalk.com/i/p/ZdN0G7l984vWzWYKPDmrgarqqyl0KGxd"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span aria-hidden="true">📖</span>
          <span><strong>连接参数获取说明</strong><small>不知道如何获取账套 ID、APP ID 和密钥？点击查看说明文档。</small></span>
          <span aria-hidden="true">↗</span>
        </a>
        <div style={styles.formItem}>
          <label style={styles.label}>
            <span style={styles.labelIcon}>📝</span>
            配置名称 <span style={styles.required}>*</span>
          </label>
          <div style={styles.inputWrapper}>
            <div style={styles.inputIconBox}>📝</div>
            <input type="text" style={styles.input} value={voucherName} onChange={(e) => setVoucherName(e.target.value)} placeholder="请输入配置名称" />
          </div>
          <div style={{ fontSize: "11px", color: "#9ca3af", marginTop: "4px" }}></div>
        </div>
        <hr style={{ ...styles.hr, margin: "16px 0" }} />
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
        <div style={styles.formItem}>
          <label style={styles.label}>
            <span style={styles.labelIcon}>⬆️</span>
            排序号 <span style={{ color: "#9ca3af", fontWeight: 400 }}>(可选)</span>
          </label>
          <div style={styles.inputWrapper}>
            <div style={styles.inputIconBox}>⬆️</div>
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

  /** 渲染表单搜索与自定义表单入口。 */
  const renderStep2 = () => (
    <div className="form-selection-page" style={{ minWidth: 0, display: 'flex', flexDirection: 'column' }}>
      <div style={styles.formSelectionHeading}>
        <span style={styles.formSelectionIcon} aria-hidden="true">📋</span>
        <div>
          <h3 style={{ margin: 0, fontSize: '17px', color: '#1d2939' }}>选择同步表单</h3>
          <p className="form-selection-help">选择需要同步的金蝶表单，下一步设置明细范围。</p>
        </div>
      </div>
      <div style={styles.formSelectionPanel}>
      {!isOtherForm ? (
        <>
          <div style={{ ...styles.formItem, position: 'relative' }}>
            <label htmlFor="sync-form-search" style={styles.label}>
              <span style={styles.labelIcon}>📋</span>
              选择表单 <span style={styles.required}>*</span>
            </label>
            <div 
              data-form-dropdown="true"
              style={{ ...styles.inputWrapper, padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center' }} 

            >
              <div style={styles.inputIconBox} aria-hidden="true">🔎</div>
              <input 
                id="sync-form-search"
                aria-expanded={formDropdownOpen}
                aria-controls="sync-form-options"
                type="text" 
                style={{ ...styles.input, padding: '10px 12px' }} 
                value={displayValue}
                placeholder="输入表单名称搜索"
                onClick={(e) => e.stopPropagation()}
                onChange={(e) => {
                  const val = e.target.value;
                  setDisplayValue(val);
                  setFormSearchText(val);
                  setFormDropdownOpen(true);
                  // 如果清空了输入，清除选中状态
                  if (!val.trim()) {
                    setSelectedFormId('');
                    setSelectedFormName('');
                  }
                }}
                onFocus={() => {
                  setFormDropdownOpen(true);
                  // 聚焦时如果有选中的表单名，清空显示让用户重新输入
                  if (selectedFormName && !formSearchText) {
                    setDisplayValue('');
                    setFormSearchText('');
                  }
                }}
                onBlur={() => {
                  // 失焦时如果没有输入搜索内容但有选中的表单，恢复显示表单名
                  if (!formSearchText && selectedFormName) {
                    setDisplayValue(selectedFormName);
                  }
                }}
              />
              <button type="button" aria-label="展开或收起表单列表"
                style={{ background: 'transparent', border: 'none', alignSelf: 'stretch', paddingLeft: '12px', paddingRight: '12px', color: '#bfbfbf', fontSize: '12px', cursor: 'pointer' }}
                onClick={(e) => {
                  e.stopPropagation();
                  setFormDropdownOpen(!formDropdownOpen);
                }}
              >
                {formDropdownOpen ? '▲' : '▼'}
              </button>
            </div>
            {formDropdownOpen && (
              <div id="sync-form-options" data-form-dropdown="true" style={{
                position: 'relative',
                top: 0,
                left: 0,
                right: 0,
                maxHeight: '200px',
                overflowY: 'auto',
                background: '#fff',
                border: '1px solid #d9d9d9',
                borderRadius: '8px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                zIndex: 1000,
                marginTop: '4px',
              }}>
                {formList
                  .filter(item => 
                    !formSearchText || 
                    item.name.toLowerCase().includes(formSearchText.toLowerCase())
                  )
                  .map((item) => (
                    <button type="button"
                      key={item.id}
                      style={{
                        width: '100%', textAlign: 'left', border: 'none', color: '#374151',
                        padding: '10px 12px',
                        cursor: 'pointer',
                        fontSize: '14px',
                        background: selectedFormId === item.id ? '#e6f4ff' : '#fff',
                        borderBottom: '1px solid #f0f0f0',
                        transition: 'background 0.2s',
                      }}
                      onMouseEnter={(e) => {
                        if (selectedFormId !== item.id) {
                          e.currentTarget.style.background = '#f5f5f5';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (selectedFormId !== item.id) {
                          e.currentTarget.style.background = '#fff';
                        }
                      }}
                      onClick={() => {
                        setSelectedFormId(item.id);
                        setSelectedFormName(item.name);
                        setDisplayValue(item.name);
                        setFormSearchText('');
                        setFormDropdownOpen(false);
                      }}
                    >
                      {item.name}{selectedFormId === item.id && <span style={{ float: 'right', color: '#1677ff' }}>✓</span>}
                    </button>
                  ))
                }
                {formList.filter(item => 
                  !formSearchText || 
                  item.name.toLowerCase().includes(formSearchText.toLowerCase())
                ).length === 0 && (
                  <div style={{ padding: '16px', textAlign: 'center', color: '#bfbfbf', fontSize: '13px' }}>
                    未找到匹配的表单
                  </div>
                )}
              </div>
            )}
          </div>
          <div className="form-selection-alternative">
            <span className="form-selection-help">列表中没有需要的表单？</span>
            <button 
              style={{ 
                background: 'none', 
                border: 'none', 
                color: '#1677ff', 
                fontSize: '14px', 
                cursor: 'pointer',
                fontWeight: 600,
                padding: '6px 0'
              }} 
              onClick={(e) => {
                e.stopPropagation();
                setIsOtherForm(true);
                setSelectedFormId('');
                setSelectedFormName('');
                setDisplayValue('');
                setFormSearchText('');
              }}
            >
              + 使用其他表单
            </button>
          </div>
        </>
      ) : (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 600, color: '#374151' }}>填写自定义表单信息</h4>
            <button 
              style={{ 
                background: 'none', 
                border: '1px solid #d9d9d9', 
                borderRadius: '6px',
                color: '#6b7280', 
                fontSize: '13px', 
                cursor: 'pointer',
                padding: '6px 14px'
              }} 
              onClick={() => {
                setIsOtherForm(false);
                setOtherFormName('');
                setOtherFormKey('');
              }}
            >
              返回选择列表
            </button>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '0' }}>
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
                <div style={styles.inputIconBox}></div>
                <input type="text" style={styles.input} value={otherFormKey} onChange={(e) => setOtherFormKey(e.target.value)} placeholder="请输入表单 ID" />
              </div>
            </div>
          </div>
        </>
      )}
      </div>
      <div style={styles.btnGroup}>
        {!savedSyncConfigRef.current && <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={handleBackToList}>返回列表</button>}
        <button 
          style={{ ...styles.button, ...styles.primaryBtn }} 
          onClick={handleStep2Next} 
          disabled={loading || (isOtherForm ? (!otherFormName.trim() || !otherFormKey.trim()) : (!selectedFormId || !selectedFormName))}
        >
          {loading ? '处理中...' : '下一步'}
        </button>
      </div>
    </div>
  );

  /** 根据选中明细生成配置字段范围，并移除已失效的筛选排序项。 */
  const handleEntryNext = () => {
    const entries = entryOptions.filter(entry => selectedEntryKeys.includes(entry.key));
    if (entries.length !== selectedEntryKeys.length || entries.some(entry => !entry.supported)) {
      showMessage('请选择可同步的明细表', 'warning');
      return;
    }
    const fields = entries.length
      ? [...headFields.map(field => ({ ...field, isPrimary: false })), ...entries.flatMap((entry, index) => entry.fields.map(field => ({ ...field, isPrimary: index === 0 && field.isPrimary })))]
      : headFields;
    const ids = new Set(fields.map(field => field.id));
    if (ids.size !== fields.length) {
      showMessage('表头与明细字段标识冲突，该表单需要适配', 'error');
      return;
    }
    fieldConfigs.forEach(config => { if (ids.has(config.fieldId) && config.outputFieldId) ids.add(config.outputFieldId); });
    const filters = filterConditions.map(group => ({ conditions: group.conditions.filter(filter => !filter.fieldId || ids.has(filter.fieldId)) }));
    const sorts = sortConfigs.filter(sort => !sort.fieldId || ids.has(sort.fieldId));
    const removed = filters.some((group, index) => group.conditions.length !== filterConditions[index].conditions.length)
      || sorts.length !== sortConfigs.length;
    setFilterConditions(filters.some(group => group.conditions.length)
      ? filters.filter(group => group.conditions.length)
      : [{ conditions: [{ fieldId: '', operator: '=', value: '' }] }]);
    setSortConfigs(sorts.length ? sorts : [{ fieldId: '', order: 'asc' }]);
    setSheetFields(fields);
    setConfigurationFields(fields);
    setFieldConfigs(configs => configs.filter(config => ids.has(config.fieldId)));
    if (removed) showMessage('已清除不属于当前明细表的筛选和排序条件', 'warning');
    setCurrentStep(3);
  };

  /** 按连接与引用对象缓存元数据请求；失败后移除缓存以便重试。 */
  const loadReferenceFields = useCallback((formId: string): Promise<ReferenceField[]> => {
    const key = JSON.stringify([kingdeeConfig, formId]);
    // 缓存键包含连接参数，切换账套后不能复用另一账套的引用元数据。
    const existing = referenceMetadataCache.current.get(key);
    if (existing) return existing;
    const request = (async () => {
      const response = await fetch(API.referenceSchema, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ formId, params: JSON.stringify(kingdeeConfig) }),
      });
      const result = await response.json();
      if (!response.ok || result.code !== 0 || !Array.isArray(result.data?.fields))
        throw new Error(result.msg || '获取引用对象元数据失败');
      return result.data.fields as ReferenceField[];
    })();
    referenceMetadataCache.current.set(key, request);
    // 失败请求不长期缓存，用户重试时应重新访问元数据接口。
    request.catch(() => referenceMetadataCache.current.delete(key));
    return request;
  }, [kingdeeConfig]);

  /** 接受服务端校验后的最终输出字段，并清理不再存在的筛选排序列。 */
  const applyConfiguredFields = (fields: SheetField[]) => {
    const ids = new Set(fields.map(field => field.id));
    setSheetFields(fields);
    setFilterConditions(groups => groups.map(group => ({ conditions: group.conditions.filter(condition => !condition.fieldId || ids.has(condition.fieldId)) }))
      .filter(group => group.conditions.length));
    setSortConfigs(sorts => sorts.filter(sort => !sort.fieldId || ids.has(sort.fieldId)));
  };

  /** 无配置时保留默认结构；有配置时交由服务端验证路径与输出类型。 */
  const handleFieldConfigNext = async () => {
    // 保持默认时直接进入筛选；有配置才请求服务端验证别名、类型和多级路径。
    if (!fieldConfigs.length) { applyConfiguredFields(configurationFields); setCurrentStep(4); return; }
    setFieldConfigBusy(true);
    try {
      const params = { ...kingdeeConfig, selectedFormId, selectedFormName, selectedEntryKeys, selectedEntryKey, headerFillMode, fieldConfigs };
      const response = await fetch(API.formSchema, { method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ params: JSON.stringify(params), context: { corpId: getCorpIdFromUrl() } }) });
      const result = await response.json();
      if (!response.ok || result.code !== 0 || !Array.isArray(result.data?.fields)) throw new Error(result.msg || '字段配置校验失败');
      applyConfiguredFields(result.data.fields);
      setCurrentStep(4);
    } catch (reason) { showMessage(reason instanceof Error ? reason.message : '字段配置校验失败', 'error'); }
    finally { setFieldConfigBusy(false); }
  };

  /** 渲染明细多选及表头填充方式。 */
  const renderEntryStep = () => (
    <div className="entry-step">
      <h3>📋 选择明细表</h3>
      <p className="entry-help">{selectedFormName} · 支持同时选择多个明细表</p>
      <fieldset className="entry-options">
        <legend>同步数据范围</legend>
        <label className={`entry-option ${!selectedEntryKeys.length ? 'is-selected' : ''}`}>
          <input type="checkbox" name="entryKey" checked={!selectedEntryKeys.length} onChange={() => { setSelectedEntryKeys([]); setSelectedEntryKey(''); }} />
          <span>不同步明细，仅同步表头</span>
        </label>
        {entryOptions.map(entry => (
          <label key={entry.key} className={`entry-option ${selectedEntryKeys.includes(entry.key) ? 'is-selected' : ''}`}>
            <input type="checkbox" name="entryKey" value={entry.key} checked={selectedEntryKeys.includes(entry.key)}
              disabled={!entry.supported} onChange={() => { const keys = selectedEntryKeys.includes(entry.key) ? selectedEntryKeys.filter(key => key !== entry.key) : [...selectedEntryKeys, entry.key]; setSelectedEntryKeys(keys); setSelectedEntryKey(keys[0] || ''); }} />
            <span>{entry.name}<small>{entry.supported ? `${Math.max(0, entry.fields.length - 1)} 个明细字段` : entry.reason}</small></span>
          </label>
        ))}
      </fieldset>
      {entryOptions.length === 0 && <p className="entry-help">该表单没有可同步的明细表，将仅同步表头。</p>}
      {selectedEntryKeys.length > 0 && (
        <fieldset className="entry-options">
          <legend>🔄 同步方式</legend>
          {selectedEntryKeys.length > 1 && <p className="entry-help">多个明细按金蝶联查结果展开，同一单据的明细组合可能重复显示，请勿直接汇总金额。</p>}
          <label className={`entry-option ${headerFillMode === 'FIRST_ROW' ? 'is-selected' : ''}`}>
            <input type="radio" name="headerFillMode" checked={headerFillMode === 'FIRST_ROW'} onChange={() => setHeaderFillMode('FIRST_ROW')} />
            <span>只有第一行包含表头数据<small>每张单据筛选后的第一条明细显示表头，其余行留空。</small></span>
          </label>
          <label className={`entry-option ${headerFillMode === 'ALL_ROWS' ? 'is-selected' : ''}`}>
            <input type="radio" name="headerFillMode" checked={headerFillMode === 'ALL_ROWS'} onChange={() => setHeaderFillMode('ALL_ROWS')} />
            <span>所有行都包含表头数据<small>每条明细都显示所属单据的表头，便于逐行筛选。</small></span>
          </label>
          <p className="entry-help">明细按单据分组，明细排序在单据内生效。同步数量按明细行计算。</p>
          <p className="entry-help">{headerFillMode === 'FIRST_ROW' ? '留空不是合并单元格，在钉钉中重新排序可能使明细与表头分开。' : '单据汇总金额会重复显示，请勿直接按明细行求和。'}</p>
        </fieldset>
      )}
      <div style={styles.btnGroup}>
        <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={() => setCurrentStep(1)}>上一步</button>
        <button style={{ ...styles.button, ...styles.primaryBtn }} onClick={handleEntryNext}>下一步</button>
      </div>
    </div>
  );

  /** 按稳定输出列标识查找当前字段定义。 */
  const getFieldById = (fieldId: string) => sheetFields.find((f) => f.id === fieldId);
  /** 判断日期条件是否使用相对日期快捷项。 */
  const isPresetDateFilter = (filter: FilterCondition) => getFieldById(filter.fieldId)?.type === 'date'
    && filter.operator === '=' && DATE_QUICK_SELECTS.some(item => item.value === filter.value);
    
    /** 按字段类型及操作符选择文本、数字、日期或枚举输入。 */
    const renderFilterValue = (filter: FilterCondition, groupIndex: number, conditionIndex: number) => {
      const field = getFieldById(filter.fieldId);
      if (isEmptyOperator(filter.operator)) return <span style={{ color: '#6b7280', fontSize: '12px', flex: 1 }}>无需填写筛选值</span>;
      if (!field) return <input type="text" style={{ ...styles.input, width: '150px', border: '1px solid #d9d9d9', borderRadius: '8px' }} value={filter.value as string} onChange={(e) => updateFilter(groupIndex, conditionIndex, 'value', e.target.value)} placeholder="筛选值" disabled />;
      
      // 日期字段:添加快速时间选择
      if (field.type === 'date') {
        const isQuickSelect = isPresetDateFilter(filter);
        
        return (
          <div className="plugin-date-value" style={{ display: 'flex', flexWrap: 'wrap', minWidth: 0, gap: '8px', alignItems: 'center' }}>
            {!isQuickSelect && (
              <input 
                type="date" 
                style={{ ...styles.input, width: '180px', border: '1px solid #d9d9d9', borderRadius: '8px' }} 
                value={filter.value as string} 
                onChange={(e) => updateFilter(groupIndex, conditionIndex, 'value', e.target.value)} 
                placeholder="选择日期"
              />
            )}
            <select 
              style={{ ...styles.select, width: '120px', fontSize: '12px', padding: '8px' }}
              value={isQuickSelect ? filter.value : ''}
              onChange={(e) => {
                const val = e.target.value;
                // 选择占位符时自动将operator设为"=",清空时恢复默认
                if (val) {
                  updateFilter(groupIndex, conditionIndex, 'operator', '=');
                }
                updateFilter(groupIndex, conditionIndex, 'value', val);
              }}
            >
              <option value="">自定义</option>
              {DATE_QUICK_SELECTS.map(item => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
          </div>
        );
      }
      
      if (field.type === 'singleSelect') {
        const choices = field.property?.choices || [];
        if (filter.operator === '包含于') return <MultiSelect options={choices} value={Array.isArray(filter.value) ? filter.value : filter.value ? [filter.value] : []} onChange={(val) => updateFilter(groupIndex, conditionIndex, 'value', val)} placeholder="请选择一个或多个选项" />;
        return <select aria-label="筛选选项" style={styles.select} value={scalarValue(filter.value)} onChange={e => updateFilter(groupIndex, conditionIndex, 'value', e.target.value)}>
          <option value="">请选择{fieldKind(field) === 'boolean' ? '是或否' : '一个选项'}</option>
          {choices.map(choice => <option key={choice.value} value={choice.value}>{choice.name}</option>)}
        </select>;
      }
      return <input type={fieldKind(field) === 'number' ? 'number' : 'text'} step="any" style={{ ...styles.input, width: '150px', border: '1px solid #d9d9d9', borderRadius: '8px' }} value={scalarValue(filter.value)} onChange={(e) => updateFilter(groupIndex, conditionIndex, 'value', e.target.value)} placeholder="筛选值" />;
    };
  /** 渲染筛选条件组与排序卡片，组内并且、组间或者。 */
  const renderStep3 = () => {
    if (fieldLoading) return <div style={styles.loading}>加载表单字段中...</div>;
    return (
      <div style={{ minWidth: 0, display: 'flex', flexDirection: 'column', paddingRight: '2px' }}>
        <div style={styles.filterSection}>
          <div style={styles.filterHeader}>
            <h4 style={styles.sectionTitle}>🔍 筛选条件</h4>
          </div>
          <div className="filter-logic-help">
            <p><strong>组内「并且」：</strong>同一组的所有条件都要满足。</p>
            <p><strong>组间「或者」：</strong>满足任意一个条件组即可。</p>
            <p className="filter-logic-example">例如：（状态 = 已审核 并且 金额 &gt; 1000）或者（客户 = 指定客户）。</p>
          </div>
          {!filterConditions.some(group => group.conditions.some(filter => filter.fieldId)) && <p className="filter-field-help">未设置筛选，将同步当前范围内的数据（受同步数量上限约束）。</p>}
          {filterConditions.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#b0b8c1', padding: '12px', fontSize: '13px', background: '#f8fafc', borderRadius: '6px', border: '1px dashed #e5e7eb' }}>暂无筛选条件</div>
          ) : filterConditions.map((group, groupIndex) => (
            <div key={groupIndex}>
              {groupIndex > 0 && (
                <div className="filter-or-divider"><span>或者（OR）</span><small>满足任意一组即可</small></div>
              )}
              <section className="filter-group-card" aria-labelledby={`filter-group-${groupIndex}`}>
                <div className="filter-group-heading">
                  <h5 id={`filter-group-${groupIndex}`}>条件组 {groupIndex + 1}</h5>
                  <span className="filter-and-badge">并且（AND）· 全部满足</span>
                </div>
              {/* 当前组内的所有条件(AND关系) */}
              {group.conditions.map((filter, conditionIndex) => (
                <div key={conditionIndex}>
                  {conditionIndex > 0 && <div className="filter-and-connector">并且（AND）</div>}
                  <div className="plugin-condition" style={styles.conditionRow}>
                    <SearchableFieldSelect id={`filter-field-${groupIndex}-${conditionIndex}`} fields={sheetFields}
                      value={filter.fieldId} onChange={value => {
                        updateFilter(groupIndex, conditionIndex, 'fieldId', value);
                        updateFilter(groupIndex, conditionIndex, 'operator', '=');
                        updateFilter(groupIndex, conditionIndex, 'value', '');
                      }} />
                    {/* 预设日期范围已确定比较逻辑，自定义日期才显示操作符。 */}
                    {!isPresetDateFilter(filter) && <select aria-label="筛选操作符" disabled={!filter.fieldId} style={{ ...styles.select, width: '120px' }} value={filter.operator} onChange={(e) => {
                      const op = e.target.value;
                      updateFilter(groupIndex, conditionIndex, 'operator', op);
                      if (isEmptyOperator(op)) updateFilter(groupIndex, conditionIndex, 'value', '');
                      else if (op === '包含于') updateFilter(groupIndex, conditionIndex, 'value', Array.isArray(filter.value) ? filter.value : filter.value ? [filter.value] : []);
                      else if (Array.isArray(filter.value)) updateFilter(groupIndex, conditionIndex, 'value', scalarValue(filter.value));
                      else if (getFieldById(filter.fieldId)?.type === 'date' && DATE_QUICK_SELECTS.some(q => q.value === filter.value)) updateFilter(groupIndex, conditionIndex, 'value', '');
                    }}>
                      {!filterOperators(getFieldById(filter.fieldId)).includes(filter.operator) && <option value={filter.operator}>{operatorLabels[filter.operator] || filter.operator}（历史配置）</option>}
                      {filterOperators(getFieldById(filter.fieldId)).map(op => <option key={op} value={op}>{operatorLabels[op] || op}</option>)}
                    </select>}
                    {renderFilterValue(filter, groupIndex, conditionIndex)}
                    <button style={{ ...styles.button, ...styles.dangerBtn }} onClick={() => removeFilter(groupIndex, conditionIndex)}>删除</button>
                  </div>
                  {filter.fieldId && <div className="filter-field-help">
                    {!filterOperators(getFieldById(filter.fieldId)).includes(filter.operator) && <span>保留历史操作符；新条件请使用推荐操作符。</span>}
                    {filter.operator === '不包含' && <span>不包含不匹配 NULL；如需空值，请添加“为空”的或者组。</span>}
                    {getFieldById(filter.fieldId)?.type === 'date' && <span>按自然日筛选；快捷范围在每次同步时重新计算。</span>}
                    {filterError(filter, getFieldById(filter.fieldId)) && <span className="filter-field-error" role="status">{filterError(filter, getFieldById(filter.fieldId))}</span>}
                  </div>}
                </div>
              ))}
              
              {group.conditions.length === 0 && <p className="filter-group-empty">本组暂无条件，请添加条件。</p>}
              <button type="button" className="filter-add-and" onClick={() => addFilter(groupIndex)}>
                + 添加组内条件（并且）
              </button>
              </section>
            </div>
          ))}
          <div className="filter-add-group">
            <button type="button" className="filter-add-or" onClick={addOr}>+ 添加条件组（或者）</button>
            <span>新增一组可独立满足的筛选条件</span>
          </div>
        </div>
        <div style={styles.filterSection}>
          <div style={styles.filterHeader}>
            <h4 style={styles.sectionTitle}>↕️ 排序字段</h4>
            <button style={{ ...styles.button, ...styles.defaultBtn, padding: '4px 12px', fontSize: '12px' }} onClick={addSort} disabled={sheetFields.length === 0}>+ 添加排序</button>
          </div>
          {sortConfigs.length === 0 ? <div style={{ textAlign: 'center', color: '#b0b8c1', padding: '12px', fontSize: '13px', background: '#f8fafc', borderRadius: '6px', border: '1px dashed #e5e7eb' }}>暂无排序条件，点击右侧按钮添加</div> : sortConfigs.map((sort, index) => (
            <div key={index} className="plugin-condition" style={styles.conditionRow}>
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
          <button style={{ ...styles.button, ...styles.defaultBtn }} onClick={() => setCurrentStep(3)}>返回</button>
          <button style={{ ...styles.button, ...styles.primaryBtn }} onClick={handleSubmit} disabled={loading}>{loading ? '提交中...' : '提交配置'}</button>
        </div>
      </div>
    );
  };

  if (syncConfigLoading || syncConfigError) {
    return <div className="plugin-shell" style={styles.container}>
      <div className="plugin-version" aria-label="前端版本">版本 v{APP_VERSION}</div>
      <main className="plugin-page" style={styles.card}>
        {syncConfigLoading ? <p role="status">正在读取同步设置...</p> : <>
          <p role="alert">{syncConfigError}</p>
          <button style={{ ...styles.button, ...styles.primaryBtn }} onClick={() => void restoreSavedSyncConfig()}>重新读取</button>
        </>}
      </main>
    </div>;
  }

  if (viewMode === "list") {
    return (
      <div className="plugin-shell" style={styles.container}>
      <div className="plugin-version" aria-label="前端版本">版本 v{APP_VERSION}</div>
        {renderPayDialog()}
        {renderChannelDialog()}
        {renderToast()}
        {upgradeDialogVisible ? (
          renderUpgradeDialog()
        ) : (
          <main className="plugin-page" style={styles.card}>
            {renderProductInfo()}
            {renderVoucherList()}
          </main>
        )}
      </div>
    );
  }

  return (
    <div className="plugin-shell" style={styles.container}>
      <div className="plugin-version" aria-label="前端版本">版本 v{APP_VERSION}</div>
      {renderPayDialog()}
      {renderChannelDialog()}
      {renderToast()}
      {upgradeDialogVisible ? (
        renderUpgradeDialog()
      ) : (
        <main className={`plugin-page${currentStep === 3 ? ' field-config-page' : ''}`} style={styles.card}>
          {currentStep <= 1 && renderProductInfo()}
          {currentStep === 0 && renderStep1()}
          {currentStep === 1 && renderStep2()}
          {currentStep === 2 && renderEntryStep()}
          {currentStep === 3 && <FieldConfigurationStep fields={configurationFields} configs={fieldConfigs}
            onChange={setFieldConfigs} loadFields={loadReferenceFields} busy={fieldConfigBusy}
            onBack={() => setCurrentStep(2)} onNext={handleFieldConfigNext} />}
          {currentStep === 4 && renderStep3()}
        </main>
      )}
    </div>
  );
}

export default App;
